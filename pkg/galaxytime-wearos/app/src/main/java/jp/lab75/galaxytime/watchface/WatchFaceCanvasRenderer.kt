package jp.lab75.galaxytime

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import android.view.SurfaceHolder

import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.TapType
import androidx.wear.watchface.WatchFace.TapListener
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import jp.lab75.galaxytime.data.watchface.ColorStyleIdAndResourceIds
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import jp.lab75.galaxytime.data.watchface.WatchMode

import jp.lab75.galaxytime.service.Calculations
import jp.lab75.galaxytime.service.Data
import jp.lab75.galaxytime.service.MeetingService
import jp.lab75.galaxytime.service.getReferenceDataFromThemeName

import jp.lab75.galaxytime.utils.COLOR_STYLE_SETTING
import jp.lab75.galaxytime.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import jp.lab75.galaxytime.utils.drawGradientArc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import jp.lab75.galaxytime.service.BiometricsService
import jp.lab75.galaxytime.views.CompassActivity
import jp.lab75.galaxytime.views.AstronomicsActivity
import jp.lab75.galaxytime.views.BiometricsActivity
import kotlin.math.roundToInt

//	shaders

// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

class WatchFaceCanvasRenderer (
	private val context: Context,
	surfaceHolder: SurfaceHolder,
	watchState: WatchState,
	private val complicationSlotsManager: ComplicationSlotsManager,
	currentUserStyleRepository: CurrentUserStyleRepository,
	canvasType: Int,
	private val calculations: Calculations,
	private val meetingService: MeetingService,
	private val biometricsService: BiometricsService,
) : Renderer.CanvasRenderer2<WatchFaceCanvasRenderer.AnalogSharedAssets>(
	surfaceHolder,
	currentUserStyleRepository,
	watchState,
	canvasType,
	FRAME_PERIOD_MS_DEFAULT,
	clearWithBackgroundTintBeforeRenderingHighlightLayer = false,
), TapListener {

	class AnalogSharedAssets : SharedAssets {
		override fun onDestroy() {}
	}

	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
	private val resources: Resources = context.resources
	private var watchFaceData: WatchFaceData = WatchFaceData()
	private var watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	private var themeName = watchFaceData.activeColorStyle.toString()
	private var currentWatchFaceSize = screenBounds // Rect(0, 0, 450, 450)
	private var watchMode: WatchMode = WatchMode.WATCH

	private val TL = Rect(0,0,currentWatchFaceSize.exactCenterX().toInt(),currentWatchFaceSize.exactCenterY().toInt())
	private val TR = Rect(currentWatchFaceSize.exactCenterX().toInt(),0,currentWatchFaceSize.width(),currentWatchFaceSize.exactCenterY().toInt())
	private val BL = Rect(0,currentWatchFaceSize.exactCenterY().toInt(),currentWatchFaceSize.exactCenterX().toInt(),currentWatchFaceSize.height())
	private val BR = Rect(currentWatchFaceSize.exactCenterX().toInt(),currentWatchFaceSize.exactCenterY().toInt(),currentWatchFaceSize.width(),currentWatchFaceSize.height())

//	private var transitionAlpha = 0f

	private fun openActivity(view: String ) {
		Log.d(TAG, "openCompassActivity()")
		val intent = when(view) {
			"COMPASS" -> Intent(context, CompassActivity::class.java)
			"ASTRONOMICS" -> Intent(context, AstronomicsActivity::class.java)
			"BIOMETRICS" -> Intent(context, BiometricsActivity::class.java)
			else -> return
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		val b = Bundle()
			b.putString("name", this.watchFaceData.activeColorStyle.toString() )
			b.putInt("color", this.watchFaceColors.activePrimaryColor )
		intent.putExtras( b )
		context.applicationContext.startActivity( intent )
	}

	@SuppressLint("RestrictedApi")
	override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {

		if (tapType == TapType.UP) {
			val x = tapEvent.xPos
			val y = tapEvent.yPos
			if (TL.contains(x, y)) openActivity("ASTRONOMICS")	// details for current planet
			if (TR.contains(x, y)) openActivity("COMPASS")		// find selected planet
			if (BL.contains(x, y)) openActivity("BIOMETRICS")		// show hydration activity
//			if (BR.contains(x, y)) Log.d("TAP", "BR")
		}
		invalidate()

	}

	private val default_fontsize = 20f
	private val small_fontsize = 12f

	// general text
	private val textPaint = Paint().apply {
		isAntiAlias = true
		color = Color.YELLOW
		textSize = default_fontsize
		isSubpixelText = true
		typeface = resources.getFont(R.font.b612)
	}

	// location zone top
	private var p1 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
		textSize = default_fontsize
		textAlign = Paint.Align.CENTER
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont(R.font.b612)
	}

	// time zones left
	private var p2 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
		textSize = default_fontsize
		textAlign = Paint.Align.RIGHT
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont(R.font.b612)
	}

	private var p3 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
		textSize = small_fontsize
		textAlign = Paint.Align.RIGHT
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont(R.font.b612_bold)
	}

	private var blendPaint = Paint().apply {
		blendMode = BlendMode.OVERLAY
	}

	// grain overlay

	private var grainBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.sgt_grain)
	private var grainImage: Bitmap = Bitmap.createScaledBitmap(
		grainBitmap,
		currentWatchFaceSize.width(),
		currentWatchFaceSize.height(),
		false
	)

	// init

	init {
		scope.launch {
			currentUserStyleRepository.userStyle.collect {
				userStyle -> updateWatchFaceData(userStyle)
			}
		}
	}

	override suspend fun createSharedAssets(): AnalogSharedAssets {
		return AnalogSharedAssets()
	}

	//	update theme

	private fun updateWatchFaceData(userStyle: UserStyle) {
		Log.d(TAG, "updateWatchFace(): $userStyle")
		themeName = watchFaceData.activeColorStyle.toString()

		if (currentWatchFaceSize.width() != 0) {
			grainImage = Bitmap.createScaledBitmap(
				grainBitmap,
				currentWatchFaceSize.width(),
				currentWatchFaceSize.height(),
				false
			)
		}

		p1.setShadowLayer(10f, 0f, 0f, Color.BLACK)
		p2.setShadowLayer(10f, 0f, 0f, Color.BLACK)
		textPaint.color = watchFaceColors.activePrimaryColor

		var newWatchFaceData: WatchFaceData = watchFaceData

		// Loops through user style and applies new values to watchFaceData.
		for (options in userStyle) {

			when (options.key.id.toString()) {

				COLOR_STYLE_SETTING -> {
					val listOption = options.value as
						UserStyleSetting.ListUserStyleSetting.ListOption

					newWatchFaceData = newWatchFaceData.copy(
						activeColorStyle = ColorStyleIdAndResourceIds.getColorStyleConfig(
							listOption.id.toString()
						)
					)
				}

//				DRAW_HOUR_PIPS_STYLE_SETTING -> {
//					val booleanValue = options.value as
//						UserStyleSetting.BooleanUserStyleSetting.BooleanOption

//					newWatchFaceData = newWatchFaceData.copy(
//						drawHourPips = booleanValue.value
//					)
//				}

				// WATCH_HAND_LENGTH_STYLE_SETTING -> {
				// }

			}
		}

		// Only updates if something changed.
		if (watchFaceData != newWatchFaceData) {
			watchFaceData = newWatchFaceData
			Log.d(TAG, watchFaceData.activeColorStyle.toString())
			// Recreates Color and ComplicationDrawable from resource ids.
			watchFaceColors = convertToColorPalette(
				context,
				watchFaceData.activeColorStyle,
				watchFaceData.ambientColorStyle
			)
			calculations.setName(watchFaceData.activeColorStyle.toString())

			// Applies the user chosen complication color scheme changes. ComplicationDrawables for
			// each of the styles are defined in XML so we need to replace the complication's
			// drawables.
			for ((_, complication) in complicationSlotsManager.complicationSlots) {
				ComplicationDrawable.getDrawable(
					context,
					watchFaceColors.complicationStyleDrawableId
				)?.let {
					(complication.renderer as CanvasComplicationDrawable).drawable = it
				}
			}

		}
		Log.d(TAG,"trigger data update")
		calculations.update()
	}

	//	destroy

	override fun onDestroy() {
		Log.d(TAG, "onDestroy()")
		scope.cancel("GalaxyWatchCanvasRenderer scope clear() request")
		super.onDestroy()
	}

	//	complication highlights

	override fun renderHighlightLayer(
		canvas: Canvas,
		bounds: Rect,
		zonedDateTime: ZonedDateTime,
		sharedAssets: AnalogSharedAssets
	) {

		canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)

		for ((_, complication) in complicationSlotsManager.complicationSlots) {
			if (complication.enabled) {
				complication.renderHighlightLayer(canvas, zonedDateTime, renderParameters)
			}
		}

	}

	//	main render loop

	override fun render(
		canvas: Canvas,
		bounds: Rect,
		zonedDateTime: ZonedDateTime,
		sharedAssets: AnalogSharedAssets
	) {

		if (currentWatchFaceSize != bounds) currentWatchFaceSize = bounds

		val interactive = ( renderParameters.drawMode == DrawMode.INTERACTIVE )
		val ambient = ( renderParameters.drawMode == DrawMode.AMBIENT )

		if ( !interactive && ambient ) {

			// TODO: implement day of year
			val dr = -90f + zonedDateTime.dayOfYear * 360f / 365f
			drawGradientArc( canvas, bounds, 0f, bounds.width() / 2 - 30f, dr, 365f, watchFaceColors.activePrimaryColor, 64 )

		} else if ( interactive && !ambient ) {

			renderWatchView(canvas, bounds, zonedDateTime)
			drawGradient(canvas, currentWatchFaceSize)
			if (currentWatchFaceSize.width() != 0) canvas.drawBitmap(grainImage, bounds, bounds, blendPaint)

			val name = watchFaceData.activeColorStyle.toString()
			drawBezel(canvas, bounds, name, watchFaceColors.activePrimaryColor)
			drawTextZones(canvas, bounds, name, zonedDateTime)

		}

	}

	private fun drawGradient(canvas: Canvas, bounds: Rect) {

		val colors = intArrayOf(
			0x00000000,
			0x00000000,
			0x00000000,
			0xFF000000.toInt(),
		)
		val stops = listOf(0f, 0.35f, 0.7f, 1f).toFloatArray()
		val circularGradientPaint = Paint().apply {
			isAntiAlias = true
			alpha = 64
			shader = RadialGradient(
				//start
				bounds.exactCenterX(),
				bounds.exactCenterY(),
				bounds.width() / 2f - 20f,
				// color table
				colors,
				stops,
				Shader.TileMode.CLAMP
			)
			// blendMode = BlendMode.OVERLAY
		}

		canvas.drawRect(
			0f,
			0f,
			bounds.width().toFloat(),
			bounds.height().toFloat(),
			circularGradientPaint
		)

	}

	private fun renderWatchView(
		canvas: Canvas,
		bounds: Rect,
		zonedDateTime: ZonedDateTime
	) {

		if (
			watchFaceData.drawComplications &&
			renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)
		) {
			drawComplications(canvas, zonedDateTime)
		}
		drawClockHands(canvas, bounds, zonedDateTime)

	}

	private fun drawTextZones(
		canvas: Canvas,
		bounds: Rect,
		name: String,
		zonedDateTime: ZonedDateTime
	) {

		val xc = bounds.exactCenterX()
		val yc = bounds.exactCenterY()
		val off1 = 25f
		val off2 = 30f

		val zero = "" // special character for zero
		val timeFormat = "%03dT%02d:%02d"

		val body = calculations.getBodyFromThemeName(name)
		var data: Data? = null
		data = calculations.getBodyData( body )
		if ( data == null ) return


		val localTime = if ( name == "EARTH" ) {
			DateTimeFormatter.ofPattern("DDD:HH:mm").format(zonedDateTime).replace("0", zero)
		} else {
			timeFormat.format(
				data.solarDay,
				data.rightAscension.degrees,
				data.rightAscension.minutes
			).replace( "0", zero )
		}

		val universalTime = DateTimeFormatter
				.ofPattern("HH:mm").format(OffsetDateTime.now())
				.replace("0", zero)


		val zoneOffset = DateTimeFormatter
			.ofPattern("O").format(OffsetDateTime.now())

		// draw location name
		canvas.drawText(name, xc, yc - 80f, p1)

		// draw timezones
		canvas.save()
		canvas.translate(85f,80f)
		p3.textAlign = Paint.Align.LEFT
		canvas.drawText("LT", xc - off1, yc - 6f, p3)
		canvas.drawText(zoneOffset, xc - off1, yc + 20f, p3)
		canvas.drawText(localTime, xc - off2, yc - 6f, p2)
		canvas.drawText(universalTime, xc - off2, yc + 20f, p2)
		canvas.restore()

	}

	private fun drawBezel(
		canvas: Canvas,
		bounds: Rect,
		themeName: String,
		themeColor: Int
	) {

		val data = calculations.getDataFromName(themeName)
		val ref = getReferenceDataFromThemeName(themeName)

		val r = ref.radiusKm
		val solDay = ref.totalRotationTimeHours
		val tl = "SOL ${solDay}h · R ${r.toInt()}km".replace("0", "")

		val dist = data?.distance ?: 0
		val tr = if ( themeName != "EARTH" ) "AZI ${data?.horizontal?.azimuth?.roundToInt()?.or(0)}° · ALT ${data?.horizontal?.altitude?.roundToInt()?.or(-2)}° · DST ${dist}AU".replace("0", "")
		else "DST ${dist}AU".replace("0", "")

		val bl = "HYDRATION ${biometricsService.state.value}"

		val nextMeeting = meetingService.getNextMeeting()
		val br =  if (nextMeeting != null) nextMeeting.title else ""

		val offset = 21f // offset from border of lunette
		val arcOffset = 2f // leap between zone arcs
		val rect = RectF(
			offset,
			offset,
			bounds.width().toFloat() - offset,
			bounds.height().toFloat() - offset
		)

		textPaint.textSize = 16f
		textPaint.color = themeColor
		textPaint.textAlign = Paint.Align.CENTER

		// top right -------------------------------------------------------------------

		val t0 = tr
		val p0 = Path()
		p0.addArc(rect, -90f + arcOffset, 90f - arcOffset)
		canvas.drawTextOnPath(t0, p0, 0f, 0f, textPaint)

		// bottom right ----------------------------------------------------------------

		var t1 = br
		val p1 = Path()
		p1.addArc(rect, 90f - arcOffset, -90f + arcOffset)
		canvas.drawTextOnPath(t1, p1, 0f, 12f, textPaint)

		// bottom left ----------------------------------------------------------------

		val t2 = bl
		val p2 = Path()
		p2.addArc(rect, 180f - arcOffset, -90f + arcOffset)
		canvas.drawTextOnPath(t2, p2, 0f, 12f, textPaint)

		// top left --------------------------------------------------------------------



		val t3 = tl
		val p3 = Path()
		p3.addArc(rect, 180f + arcOffset, 90f - arcOffset)
		canvas.drawTextOnPath(t3, p3, 0f, 0f, textPaint)

	}

	private fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {

		for ((_, complication) in complicationSlotsManager.complicationSlots) {
			if (complication.enabled) {
				complication.render(canvas, zonedDateTime, renderParameters)
			}
		}

	}

	private fun drawClockHands(
		c: Canvas,
		b: Rect,
		time: ZonedDateTime
	) {

		val name = watchFaceData.activeColorStyle.toString()
		val body = calculations.getBodyFromThemeName(name)
		var data: Data? = null

		var ss = 0f
		var mm = 0f
		var hh = 0f
		var dd = 0

		data = calculations.getBodyData(body)
		if(data!=null) {
			ss = data.rightAscension.seconds.toFloat()
			mm = data.rightAscension.minutes.toFloat()
			hh = data.rightAscension.degrees.toFloat()
			dd = if ( name.lowercase() == "earth" ) time.dayOfYear else data.solarDay
		}

		// remote
		val sr = -90f +  6f * ss
		val mr = -90f +  6f * mm
		val hr = -90f + 15f * hh
		val dr = -90f + dd * ( 360f / 365f )
//		Log.d(TAG, "drawClockHands: $dr $hr $mr $sr")

		if ( renderParameters.drawMode != DrawMode.AMBIENT ) {

			val bezelWidth = 30f
			val dialWidth = 15f
			val dialGap = 0f

			var ro = (b.width().toFloat() / 2f) - bezelWidth
			var ri = (b.width().toFloat() / 2f) - bezelWidth - dialWidth

			drawGradientArc( c, b, ri, ro, sr, 60f, watchFaceColors.activePrimaryColor, 255 )

			ro = ri
			ri -=  dialWidth
			drawGradientArc( c, b, ri, ro - dialGap, mr, 60f, watchFaceColors.activePrimaryColor, 255 )

			ro = ri
			ri -= dialWidth
			drawGradientArc( c, b, ri, ro - dialGap, hr, 24f, watchFaceColors.activePrimaryColor, 255 )

			ro = ri
			ri = 0f
			drawGradientArc( c, b, ri, ro - dialGap, dr, 365f, watchFaceColors.activePrimaryColor, 255 )

			drawExtras( c, b, name,( b.width() / 2 ) - bezelWidth.toInt() )
		}
	}

	// overlay gradients and add ons

	 private var ringPaint: Paint = Paint( Paint.ANTI_ALIAS_FLAG )

	 private var saturnRingBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.sgt_saturn_ring)
	 private var saturnRingImage: Bitmap = Bitmap.createScaledBitmap(saturnRingBitmap, currentWatchFaceSize.width(), 4, false)
	 private var saturnGradientBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.sgt_saturn_gradient)
	 private var saturnGradientImage: Bitmap = Bitmap.createScaledBitmap(saturnGradientBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false)

	 private var jupiterGradientBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.sgt_jupiter_gradient)
	 private var jupiterGradientImage: Bitmap = Bitmap.createScaledBitmap(jupiterGradientBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false)

	 private fun drawExtras( c: Canvas, b: Rect, n: String, r: Int ) {

	 	if( n == "SATURN") {
	 		val ex = b.exactCenterX().toInt()
	 		val ey = b.exactCenterY().toInt()
	 		val r1: Rect = Rect( ex - r, ey - r, ex + r, ey + r )
	 		c.drawBitmap(saturnGradientImage, b, r1, blendPaint)
	 		val r2: Rect = Rect( ex - r, ey - 2, ex + r, ey + 2 )
	 		c.drawBitmap(saturnRingImage, b, r2, ringPaint)
	 		return
	 	}

	 	if( n == "JUPITER") {
	 		val r1: Rect = Rect( b.centerX() - r, b.centerY() - r, b.centerX() + r, b.centerY() + r )
	 		c.drawBitmap(jupiterGradientImage, b, r1, blendPaint)
	 		return
	 	}

	 }

	companion object {
		private const val TAG = "Watchface"
	}
}
