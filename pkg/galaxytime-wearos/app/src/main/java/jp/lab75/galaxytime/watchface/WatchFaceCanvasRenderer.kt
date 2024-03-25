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
	private var currentWatchFaceSize = screenBounds

	private var watchFaceData: WatchFaceData = WatchFaceData()
	private var watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)
	private var themeName: String = watchFaceData.activeColorStyle.name

	private val TL = Rect(0,0,currentWatchFaceSize.exactCenterX().toInt(),currentWatchFaceSize.exactCenterY().toInt())
	private val TR = Rect(currentWatchFaceSize.exactCenterX().toInt(),0,currentWatchFaceSize.width(),currentWatchFaceSize.exactCenterY().toInt())
	private val BL = Rect(0,currentWatchFaceSize.exactCenterY().toInt(),currentWatchFaceSize.exactCenterX().toInt(),currentWatchFaceSize.height())
	private val BR = Rect(currentWatchFaceSize.exactCenterX().toInt(),currentWatchFaceSize.exactCenterY().toInt(),currentWatchFaceSize.width(),currentWatchFaceSize.height())

	private lateinit var grainImage: Bitmap
	private lateinit var saturnRingImage: Bitmap
	private lateinit var saturnGradientImage: Bitmap
	private lateinit var jupiterGradientImage: Bitmap

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

	private val defaultFontSize = 20f
	private val smallFontSize = 12f

	// general text
	private val textPaint = Paint().apply {
		isAntiAlias = true
		color = Color.YELLOW
		textSize = defaultFontSize
		isSubpixelText = true
		typeface = resources.getFont(R.font.b612)
	}

	// location zone top
	private var p1 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
		textSize = defaultFontSize
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
		textSize = defaultFontSize
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
		textSize = smallFontSize
		textAlign = Paint.Align.RIGHT
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont(R.font.b612_bold)
	}

	private var blendPaint = Paint().apply {
		blendMode = BlendMode.OVERLAY
	}

	// init

	init {
		scope.launch {
			Log.d(TAG,"INIT ${currentUserStyleRepository.userStyle.value}")
			currentUserStyleRepository.userStyle.collect {
				userStyle -> updateWatchFaceData(userStyle)
			}
		}
	}

	override suspend fun createSharedAssets(): AnalogSharedAssets {
		return AnalogSharedAssets()
	}

	//	update theme
	private fun loadImages(themeName: String) {
		when (themeName) {
			"SATURN" -> if (!::saturnRingImage.isInitialized || !::saturnGradientImage.isInitialized) {
				saturnRingImage = Bitmap.createScaledBitmap(
					BitmapFactory.decodeResource(resources, R.drawable.sgt_saturn_ring),
					currentWatchFaceSize.width(),
					4,
					true
				)
				saturnGradientImage = Bitmap.createScaledBitmap(
					BitmapFactory.decodeResource(resources, R.drawable.sgt_saturn_gradient),
					currentWatchFaceSize.width(),
					currentWatchFaceSize.height(),
					true
				)
			}
			"JUPITER" -> if (!::jupiterGradientImage.isInitialized) {
				jupiterGradientImage = Bitmap.createScaledBitmap(
					BitmapFactory.decodeResource(resources, R.drawable.sgt_jupiter_gradient),
					currentWatchFaceSize.width(),
					currentWatchFaceSize.height(),
					true
				)
			}
		}

		if (!::grainImage.isInitialized) {
			grainImage = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(resources, R.drawable.sgt_grain),
				currentWatchFaceSize.width(),
				currentWatchFaceSize.height(),
				true
			)
		}
	}

	private fun updateWatchFaceData(userStyle: UserStyle) {

		Log.d(TAG, "updateWatchFace(): ${userStyle}")

		if (currentWatchFaceSize.width() != 0) {
			loadImages(themeName)
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
			}
		}

		if (watchFaceData != newWatchFaceData && newWatchFaceData.activeColorStyle.name != "AMBIENT") {
			watchFaceData = newWatchFaceData
			watchFaceColors = convertToColorPalette(
				context,
				watchFaceData.activeColorStyle,
				watchFaceData.ambientColorStyle
			)
			themeName = watchFaceData.activeColorStyle.name
			calculations.setName(newWatchFaceData.activeColorStyle.toString())

		} else {
			Log.d(TAG, "skipping...")
		}
		calculations.update()
	}

	//	destroy

	override fun onDestroy() {
		Log.d(TAG, "onDestroy()")
		scope.cancel("$TAG scope clear() request")
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

//		for ((_, complication) in complicationSlotsManager.complicationSlots) {
//			if (complication.enabled) {
//				complication.renderHighlightLayer(canvas, zonedDateTime, renderParameters)
//			}
//		}

	}

	//	main render loop

	override fun render(
		canvas: Canvas,
		bounds: Rect,
		zonedDateTime: ZonedDateTime,
		sharedAssets: AnalogSharedAssets
	) {

		if (currentWatchFaceSize != bounds) currentWatchFaceSize = bounds
		val name = watchFaceData.activeColorStyle.toString()

		when (renderParameters.drawMode) {

			DrawMode.AMBIENT -> {
				// TODO: implement day of year
				val dr = -90f + zonedDateTime.dayOfYear * 360f / 365f
				drawGradientArc( canvas, bounds, 0f, bounds.width() / 2 - 30f, dr, 365f, watchFaceColors.activePrimaryColor, 64 )
			}

			else -> {
				drawClockHands(canvas, bounds, zonedDateTime)
				drawGradient(canvas, currentWatchFaceSize)
				if (currentWatchFaceSize.width() != 0 && ::grainImage.isInitialized)
					canvas.drawBitmap(grainImage, bounds, bounds, blendPaint)
				drawBezel(canvas, bounds, name, watchFaceColors.activePrimaryColor)
				drawTextZones(canvas, bounds, name, zonedDateTime)
			}

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
		}

		canvas.drawRect(
			0f,
			0f,
			bounds.width().toFloat(),
			bounds.height().toFloat(),
			circularGradientPaint
		)

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

		val totalAngle = 360f
		val totalDays = data.totalSolarDays
		val currentAngle = calculations.dayOfYear.value.toFloat()
		val currentDay = ( currentAngle * ( totalDays / totalAngle ) ).toInt()

		// TODO: validate calculation. lgtm.

		val localTime = if ( name == "EARTH" ) {
			timeFormat.format(
				zonedDateTime.dayOfYear,
				zonedDateTime.hour,
				zonedDateTime.minute
			).replace("0", zero)
		} else {
			timeFormat.format(
				currentDay, //data.solarDay,
				calculations.localTime.value.hh,
				calculations.localTime.value.mm,
				calculations.localTime.value.ss, // for debug
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

		// TODO: what happens on tap?
		// TODO: calc remaining time in hh:mm
		val nextMeeting = meetingService.getNextMeeting()
		val br =  if (nextMeeting != null) "$nextMeeting.title: $nextMeeting.timeRemain" else ""

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

	private fun drawClockHands(
		c: Canvas,
		b: Rect,
		time: ZonedDateTime
	) {

		val name = watchFaceData.activeColorStyle.toString()

		val sr = -90f +  6f * calculations.localTime.value.ss.toFloat()
		val mr = -90f +  6f * calculations.localTime.value.mm.toFloat()
		val hr = -90f + 15f * calculations.localTime.value.hh.toFloat()
		val dr = if ( name.lowercase() == "earth" ) {
			-90f +  time.dayOfYear * (360f / 365f)
		} else {
			-90f + calculations.localTime.value.dd
		}
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

	 private fun drawExtras( c: Canvas, b: Rect, n: String, r: Int ) {
		 when (n) {
			 "SATURN" -> {
				 if (!::saturnGradientImage.isInitialized || !::saturnRingImage.isInitialized) loadImages(n);
				 val ex = b.exactCenterX().toInt()
				 val ey = b.exactCenterY().toInt()
				 val r1: Rect = Rect( ex - r, ey - r, ex + r, ey + r )
				 c.drawBitmap(saturnGradientImage, b, r1, blendPaint)
				 val r2: Rect = Rect( ex - r, ey - 2, ex + r, ey + 2 )
				 c.drawBitmap(saturnRingImage, b, r2, ringPaint)

			 }
			 "JUPITER" -> {
				 if (!::jupiterGradientImage.isInitialized) loadImages(n);
				 val r1: Rect = Rect( b.centerX() - r, b.centerY() - r, b.centerX() + r, b.centerY() + r )
				 c.drawBitmap(jupiterGradientImage, b, r1, blendPaint)
			 }
		 }
	 }

	companion object {
		private const val TAG = "Watchface"
	}
}
