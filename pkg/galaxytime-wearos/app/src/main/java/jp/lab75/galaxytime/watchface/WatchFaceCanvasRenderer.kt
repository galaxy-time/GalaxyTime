package jp.lab75.galaxytime

// import jp.lab75.galaxytime.utils.WATCH_HAND_LENGTH_STYLE_SETTING

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
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
import jp.lab75.galaxytime.calculations.Calculations
import jp.lab75.galaxytime.data.watchface.ColorStyleIdAndResourceIds
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import jp.lab75.galaxytime.data.watchface.WatchMode
import jp.lab75.galaxytime.utils.COLOR_STYLE_SETTING
import jp.lab75.galaxytime.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import jp.lab75.galaxytime.utils.drawGradientArc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


//	shaders

// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

class WatchFaceCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int,
	private val calculations: Calculations
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

	//
	// scene transitions
	//

	var watchMode: WatchMode = WatchMode.WATCH
	var nextWatchMode: WatchMode = WatchMode.WATCH
	private enum class TransitionMode { IN, OUT, IDLE }
	private enum class TapZone { TL, TR, BL, BR }
	private var transitionMode: TransitionMode = TransitionMode.IDLE
	private var prevMode: TransitionMode = TransitionMode.IDLE
	private var transitionAlpha = 0f


	override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot? ) {

		// ON TAP UP WE TRANSITION THE VIEW TO EITHER ONE OF THE APP VIEWS OR BACK TO WATCH MODE.
		// WE SIMPLY DIVIDE THE WATCH VIEW INTO FOUR QUADRANTS TO MAKE IT EASY TO HIT.
		// NEXT VERSION CAN USE A SEPARATE CIRCULAR OBJECT IN THE MIDDLE SO WE CAN TRIGGER A
		// FIFTH VIEW.
		if ( tapType == TapType.UP ) {

			// val intent: Intent = Intent( "android.intent.action.VIEW" )
			// try {
			// 	Log.d(TAG,"intent: $intent")
			// 	startActivity( intent )
			// } catch (e: ActivityNotFoundException) {
			// 	Log.d(TAG,"action handler not found")
			// }

			//
			//
			//

			// identify compklication tap — potentially not needed
//        	var tappedComplicationId = complicationSlotsManager.getComplicationSlotAt( tapEvent.xPos, tapEvent.yPos );
//        	if ( tappedComplicationId != -1 ) {
//            	// Handle tap action
//				Log.d( TAG, "complication tapped: $tappedComplicationId" )
//        	}

			// RETURN TO WATCH MODE WHEN TAP ON LEFT TAP AREA
			if ( watchMode != WatchMode.WATCH ) {
				if ( tapEvent.xPos < currentWatchFaceSize.width() / 2 ) {
					nextWatchMode = WatchMode.WATCH
				}
			}

			// TRANSITION TO VIEW
			else {
				if ( tapEvent.xPos < currentWatchFaceSize.width() / 2 ) {
					if ( tapEvent.yPos < currentWatchFaceSize.height() / 2 ) nextWatchMode = WatchMode.ASTRONOMICS
					if ( tapEvent.yPos > currentWatchFaceSize.height() / 2 ) nextWatchMode = WatchMode.BIOMETRICS
				}
				if ( tapEvent.xPos > currentWatchFaceSize.width() / 2 ) {
					if ( tapEvent.yPos < currentWatchFaceSize.height() / 2 ) nextWatchMode = WatchMode.DIRECTIONS
					if ( tapEvent.yPos > currentWatchFaceSize.height() / 2 ) nextWatchMode = WatchMode.MOVEMENT
				}
			}

			if ( prevMode == TransitionMode.IDLE && nextWatchMode != watchMode ) fadeOut()
			// Log.d(TAG, "next mode $nextWatchMode")

		}
		invalidate()
	}

	fun fadeOut() {

		transitionMode = TransitionMode.OUT

		val animator = ValueAnimator.ofFloat(0.0f, 255f)
		animator.duration = 50

        animator.addUpdateListener { valueAnimator ->
            transitionAlpha = valueAnimator.animatedValue as Float
            invalidate()
        }
        animator.start()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                transitionAlpha = 255f
				transitionMode = TransitionMode.IDLE
				watchMode = nextWatchMode
				fadeIn()
                invalidate()
            }
        })
        animator.start()
	}

	fun fadeIn() {

		transitionMode = TransitionMode.IN

		val animator = ValueAnimator.ofFloat(255f, 0.0f)
		animator.duration = 250

        animator.addUpdateListener { valueAnimator ->
            transitionAlpha = valueAnimator.animatedValue as Float
            invalidate()
        }
        animator.start()
        animator.addListener(object : AnimatorListenerAdapter() {
			override fun onAnimationEnd(animation: Animator) {
				super.onAnimationEnd(animation)
                transitionAlpha = 0f
				transitionMode = TransitionMode.IDLE
                invalidate()
            }
        })
        animator.start()
	}

	//
	// data
	//

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var watchFaceData: WatchFaceData = WatchFaceData()
	private val resources: Resources = context.resources

	//
	//	color, text, type and size defaults
	//

	private var watchFaceColors = convertToColorPalette(
        context,
        watchFaceData.activeColorStyle,
        watchFaceData.ambientColorStyle
    )

	private var themeName = watchFaceData.activeColorStyle.toString()

	private val outerElementPaint = Paint().apply {
		isAntiAlias = true
	}

	private val clockHandPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.clock_hand_stroke_width).toFloat()
    }

	// NOPE
	// object FontUtils {
	// 	private const val FONT_PATH = "font/pp_b612_light_ttf.ttf"
	// 	fun getDefaultTypeface(context: Context): Typeface {
	// 		return Typeface.createFromAsset( context.assets, FONT_PATH )
	// 	}
	// }
	// val typeface = FontUtils.getDefaultTypeface(context)

	val default_typeface = resources.getFont( R.font.b612_mono )
	val default_fontsize = 20f
	val detail_fontsize = 16f
	val lunette_fontsize = 20f
	val small_fontsize = 12f

	// general text
	val textPaint = Paint().apply{
		isAntiAlias = true
		color = Color.YELLOW
        textSize = default_fontsize
		isSubpixelText = true
		// textPaint.letterSpacing = 0.1f
		typeface = resources.getFont( R.font.b612 )
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
		typeface = resources.getFont( R.font.b612 )
    }

	// time zones left
	var p2 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
        textSize = default_fontsize
		textAlign = Paint.Align.RIGHT
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont( R.font.b612 )
	}

	var p3 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
        textSize = small_fontsize
		textAlign = Paint.Align.RIGHT
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont( R.font.b612_mono )
	}

	var blendPaint = Paint().apply{
		blendMode = BlendMode.SRC_OVER
	}

	private var armLengthChangedRecalculateClockHands: Boolean = false
    private var currentWatchFaceSize = Rect(0,0,450,450)

	// grain overlay
	private var grainBitmap: Bitmap = BitmapFactory.decodeResource( resources, R.drawable.sgt_grain )
	private var grainImage: Bitmap = Bitmap.createScaledBitmap( grainBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false )

	//
	//
	//

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

	//
	//	update theme
	//

    private fun updateWatchFaceData(userStyle: UserStyle) {

        Log.d(TAG, "updateWatchFace(): $userStyle")

		themeName = watchFaceData.activeColorStyle.toString()
		if(currentWatchFaceSize.width()!=0) {
			grainImage = Bitmap.createScaledBitmap( grainBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false )
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

                DRAW_HOUR_PIPS_STYLE_SETTING -> {
                    val booleanValue = options.value as
                        UserStyleSetting.BooleanUserStyleSetting.BooleanOption

                    newWatchFaceData = newWatchFaceData.copy(
                        drawHourPips = booleanValue.value
                    )
                }

                // WATCH_HAND_LENGTH_STYLE_SETTING -> {
                // }

            }
        }

        // Only updates if something changed.
        if (watchFaceData != newWatchFaceData) {
            watchFaceData = newWatchFaceData

            // Recreates Color and ComplicationDrawable from resource ids.
            watchFaceColors = convertToColorPalette(
                context,
                watchFaceData.activeColorStyle,
                watchFaceData.ambientColorStyle
            )

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
    }

	//
	//	destroy
	//

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        scope.cancel("GalaxyWatchCanvasRenderer scope clear() request")
        super.onDestroy()
    }

	//
	//	complication highlights
	//

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

	//
	//	main render loop
	//


	override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: AnalogSharedAssets

    ) {

		if ( currentWatchFaceSize != bounds ) currentWatchFaceSize = bounds

		if ( renderParameters.drawMode != DrawMode.AMBIENT ) {

			when ( watchMode ) {
				WatchMode.WATCH 		-> renderWatchView(context, canvas, bounds, zonedDateTime)
				WatchMode.BIOMETRICS	-> renderBiometricsView(context, canvas, bounds, textPaint)
				WatchMode.ASTRONOMICS	-> renderAstronomicsView(context, canvas, bounds, textPaint, calculations)
				WatchMode.DIRECTIONS	-> renderDirectionsView(context, canvas, bounds, textPaint)
				WatchMode.CALENDAR		-> renderCalendarView(context, canvas, bounds, textPaint)
				WatchMode.MOVEMENT		-> renderMovementView(context, canvas, bounds, textPaint)
			}

		} else if ( renderParameters.drawMode == DrawMode.AMBIENT ) {
			val dr = -90f + zonedDateTime.dayOfYear * 360f / 365f
			drawGradientArc( canvas, bounds,   0f, 200f, dr, 365f, watchFaceColors.activePrimaryColor, 64 )
		}

		if ( watchMode == WatchMode.WATCH ) {
			drawGradient( canvas, currentWatchFaceSize )
			if(currentWatchFaceSize.width()!=0) {
				canvas.drawBitmap( grainImage, bounds, bounds, blendPaint )
			}
		}

		if ( renderParameters.drawMode != DrawMode.AMBIENT && watchMode == WatchMode.WATCH ) {
			drawLunette( canvas, bounds )
			drawTextZones(canvas, bounds, themeName, zonedDateTime )
		}

		val paint = Paint().apply { alpha = transitionAlpha.toInt() }
		canvas.drawPaint(paint)

	}

	//
	//	watch view
	//	TODO: move to views
	//

	private fun drawGradient(canvas: Canvas, bounds: Rect) {

		val colors = intArrayOf (
			0x00000000.toInt(),
			0x00000000.toInt(),
			0x00000000.toInt(),
			0xFF000000.toInt(),
		)
		val stops = listOf( 0f, 0.35f, 0.7f, 1f ).toFloatArray()
		val circularGradientPaint = Paint().apply {
		    isAntiAlias = true
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
		canvas.drawRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), circularGradientPaint)
	}

	private fun renderWatchView( context: Context, canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime ) {

		// complications
        if ( watchFaceData.drawComplications &&
			renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY) ) {
			drawComplications(canvas, zonedDateTime)
		}

		// hands
		drawClockHands(canvas, bounds, zonedDateTime)
    }

	private fun drawTapZones( canvas: Canvas, bounds: Rect ) {

		// draw four donut segments and a circle in the center
		outerElementPaint.style = Paint.Style.FILL_AND_STROKE
		outerElementPaint.alpha = 128

		val oval = RectF(0f,0f,bounds.width().toFloat(), bounds.height().toFloat())
		outerElementPaint.color = Color.RED
		canvas.drawArc(oval, 0f,90f,true, outerElementPaint)
		outerElementPaint.color = Color.GREEN
		canvas.drawArc(oval, 90f,90f,true, outerElementPaint)
		outerElementPaint.color = Color.BLUE
		canvas.drawArc(oval, 180f,90f,true, outerElementPaint)
		outerElementPaint.color = Color.YELLOW
		canvas.drawArc(oval, 270f,90f,true, outerElementPaint)
		// TODO: draw a segment in the center
		// outerElementPaint.color = Color.WHITE
		// canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), bounds.exactCenterX(), outerElementPaint )

	}

	//
	//	draw text
	//

	private fun drawTextZones( canvas: Canvas, bounds: Rect, location: String, zonedDateTime: ZonedDateTime ) {

		val xc = bounds.exactCenterX()
		val yc = bounds.exactCenterY()
		val off1 = 10f
		val off2 = 30f

		val zero = ""


		var localtime = DateTimeFormatter.ofPattern("HH:mm:ss").format(zonedDateTime).replace("0", zero)
		var universaltime = DateTimeFormatter.ofPattern("HH:mm:ss").format(OffsetDateTime.now(ZoneOffset.UTC)).replace("0", zero)

		canvas.drawText( location     , xc       , yc / 2f + 8f, p1 )
		canvas.drawText( "LT"         , xc - off1, yc - 6f     , p3 )
		canvas.drawText( "UT"         , xc - off1, yc + 20f    , p3 )
		canvas.drawText( localtime    , xc - off2, yc - 6f     , p2 )
		canvas.drawText( universaltime, xc - off2, yc + 20f    , p2 )

	}

	//
	//	draw lunette
	//

	private fun drawLunette(
		canvas: Canvas,
		bounds: Rect
	) {

		val offset = 20f // offset from border of lunette
		val arcOffset = 2f // leap between zone arcs
		val rect = RectF( offset, offset, bounds.width().toFloat() - offset, bounds.height().toFloat() - offset )

		val p = Paint().apply {
			color = Color.RED
			strokeWidth = 0.5f
			style = Paint.Style.STROKE
			typeface = typeface
			// bold arcs at bezel:
			// strokeWidth = 20f
			// strokeJoin = Paint.Join.ROUND
			// strokeCap = Paint.Cap.ROUND
		}
		p.setPathEffect( DashPathEffect( floatArrayOf(4f, 8f), 0f) )

		// val arcSweep = 90f - arcOffset - arcOffset
		// val r01 = RectF( space, space, bounds.width().toFloat() - space, bounds.height().toFloat() - space )
		// canvas.drawArc(r01, -90f + arcOffset ,arcSweep, false, p)
		// canvas.drawArc(r01,   0f + arcOffset, arcSweep, false, p)
		// canvas.drawArc(r01, 180f + arcOffset, arcSweep, false, p)
		// canvas.drawArc(r01,  90f + arcOffset, arcSweep, false, p)

		// only for debugging
		canvas.drawLine( bounds.exactCenterX(), 0f, bounds.exactCenterX(), bounds.height().toFloat(), p )
		canvas.drawLine( 0f, bounds.exactCenterY(), bounds.width().toFloat(), bounds.exactCenterY(), p )

		textPaint.textSize = 16f
		textPaint.color = Color.WHITE

		// top right
		val t0 = "1 SOL=1234 EARTH DAYS"
		val p0 = Path()
		p0.addArc( rect, -90f + arcOffset, 90f - arcOffset)
		canvas.drawTextOnPath( t0, p0, 0f, 0f, textPaint )

		// bottom right
		val t1 = "12:30 LT — ESA MEETING WITH JPL"
		val p1 = Path()
		p1.addArc( rect, 90f - arcOffset, -90f + arcOffset )
		canvas.drawTextOnPath( t1, p1, 0f, 12f, textPaint )

		// bottom left
		val t2 = "0123456789ABCDEF0123456789ABCDEF"
		val p2 = Path()
		p2.addArc( rect, 180f - arcOffset, -90f + arcOffset )
		canvas.drawTextOnPath( t2, p2, 0f, 12f, textPaint )

		// top left
		val t3 = "0123456789ABCDEF0123456789ABCDEF"
		val p3 = Path()
		p3.addArc( rect, 180f + arcOffset, 90f - arcOffset)
		canvas.drawTextOnPath( t3, p3, 0f, 0f, textPaint )

	}

	//
	//	draw complications
	//

    private fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {

        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.render(canvas, zonedDateTime, renderParameters)
            }
        }

    }

	//
	//	draw clock hands
	//

    private fun drawClockHands(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime
    ) {

		val sr = -90f + 6f * zonedDateTime.second
		val mr = -90f + 6f * zonedDateTime.minute
		val hr = -90f + 15f * zonedDateTime.hour // 24h dial = 15f, 12h dial= 30f

		// TODO: adopt to local solar year length of location
		val dr = -90f + zonedDateTime.dayOfYear * 360f / 365f

		if (
			renderParameters.drawMode != DrawMode.AMBIENT // INTERACTIVE
			// && renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE)
		) {

			val lunetteWidth = 30f
			val dialWidth = 15f
			val dialGap = 0f
			var ri = ( bounds.width().toFloat() / 2f ) - lunetteWidth - dialWidth
			var ro = ( bounds.width().toFloat() / 2f ) - lunetteWidth
			drawGradientArc( canvas, bounds, ri, ro, sr, 60f, watchFaceColors.activePrimaryColor, 255 )
			ro = ri
			ri = ri - dialWidth
			drawGradientArc( canvas, bounds, ri, ro - dialGap, mr, 60f, watchFaceColors.activePrimaryColor, 255 )
			ro = ri
			ri = ri - dialWidth
			drawGradientArc( canvas, bounds, ri, ro - dialGap, hr, 24f, watchFaceColors.activePrimaryColor, 255 )
			ro = ri
			ri = 0f
			drawGradientArc( canvas, bounds, ri, ro - dialGap, dr, 365f, watchFaceColors.activePrimaryColor, 255 )

			// TODO: interactive tap zones...
			// drawTapZones(canvas, bounds)

		}
	}

//    private fun drawNumberStyleOuterElement(
//        canvas: Canvas,
//        bounds: Rect,
//        numberRadiusFraction: Float,
//        outerCircleStokeWidthFraction: Float,
//        outerElementColor: Int,
//        numberStyleOuterCircleRadiusFraction: Float,
//        gapBetweenOuterCircleAndBorderFraction: Float
//    ) {
//
//        outerElementPaint.strokeWidth = outerCircleStokeWidthFraction * bounds.width()
//        outerElementPaint.color = outerElementColor
//        canvas.save()
//
//        for (i in 0 until 60) {
//            // if (i % 15 != 0) {
//                drawTopMiddleCircle(
//                    canvas,
//                    bounds,
//                    numberStyleOuterCircleRadiusFraction/4,
//                    gapBetweenOuterCircleAndBorderFraction
//                )
//            // }
//            canvas.rotate(360.0f / 60.0f, bounds.exactCenterX(), bounds.exactCenterY() )
//        }
//        canvas.restore()
//
//    }

    /** Draws the outer circle on the top middle of the given bounds. */
//    private fun drawTopMiddleCircle(
//        canvas: Canvas,
//        bounds: Rect,
//        radiusFraction: Float,
//        gapBetweenOuterCircleAndBorderFraction: Float
//    ) {
//
//        outerElementPaint.style = Paint.Style.FILL_AND_STROKE
//
//        val centerX = 0.5f * bounds.width().toFloat()
//        val centerY = bounds.width() * (gapBetweenOuterCircleAndBorderFraction + radiusFraction)
//
//        canvas.drawCircle(
//            centerX,
//            centerY,
//            radiusFraction * bounds.width(),
//            outerElementPaint
//        )
//
//    }

    companion object {
        private const val TAG = "CanvasRenderer"
    }
}
