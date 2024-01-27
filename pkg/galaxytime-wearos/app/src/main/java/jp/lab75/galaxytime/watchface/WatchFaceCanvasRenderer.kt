package jp.lab75.galaxytime

import android.content.Context
import android.content.res.Resources

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Color
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface

import android.util.Log
import android.view.SurfaceHolder

import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState

import androidx.wear.watchface.WatchFace.TapListener
import androidx.wear.watchface.TapType
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.ComplicationSlot

import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer

import jp.lab75.galaxytime.data.watchface.ColorStyleIdAndResourceIds
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import jp.lab75.galaxytime.data.watchface.WatchMode

import jp.lab75.galaxytime.utils.COLOR_STYLE_SETTING
import jp.lab75.galaxytime.utils.DRAW_HOUR_PIPS_STYLE_SETTING
// import jp.lab75.galaxytime.utils.WATCH_HAND_LENGTH_STYLE_SETTING

import jp.lab75.galaxytime.utils.drawGradientArc

import java.time.ZonedDateTime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ActivityNotFoundException
import android.content.Intent

//	shaders

// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

class WatchFaceCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int
) : Renderer.CanvasRenderer2<WatchFaceCanvasRenderer.AnalogSharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    canvasType,
    FRAME_PERIOD_MS_DEFAULT,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
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
					if ( tapEvent.yPos > currentWatchFaceSize.height() / 2 ) nextWatchMode = WatchMode.CALENDAR
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
	// 	private const val FONT_PATH = "font/pp_stellar_light_ttf.ttf"
	// 	fun getDefaultTypeface(context: Context): Typeface {
	// 		return Typeface.createFromAsset( context.assets, FONT_PATH )
	// 	}
	// }
	// val typeface = FontUtils.getDefaultTypeface(context)

	val typeface = resources.getFont( R.font.stellar )

	// general text
	val textPaint = Paint().apply{
		isAntiAlias = true
		color = Color.YELLOW
        textSize = 18f
		isSubpixelText = true
		// textPaint.letterSpacing = 0.1f
		typeface = resources.getFont( R.font.stellar )
	}

	// location zone top
	private var p1 = Paint().apply {
        isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
        textSize = 18f
		textAlign = Paint.Align.CENTER
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont( R.font.stellar )
    }

	// time zones left
	var p2 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
        textSize = 16f
		textAlign = Paint.Align.RIGHT
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
		typeface = resources.getFont( R.font.stellar )
	}

	private var armLengthChangedRecalculateClockHands: Boolean = false
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

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

		// val backgroundColor =
		// if ( renderParameters.drawMode == DrawMode.AMBIENT ) { watchFaceColors.ambientBackgroundColor }
		// else {	watchFaceColors.activeBackgroundColor }
        // canvas.drawColor( backgroundColor )

		if ( renderParameters.drawMode != DrawMode.AMBIENT ) {

			when ( watchMode ) {
				//
				WatchMode.WATCH -> renderWatchView(context, canvas, bounds, zonedDateTime)
				//
				WatchMode.BIOMETRICS	-> renderBiometricsView(context, canvas, bounds, textPaint)
				WatchMode.ASTRONOMICS	-> renderAstronomicsView(context, canvas, bounds, textPaint)
				WatchMode.DIRECTIONS	-> renderDirectionsView(context, canvas, bounds, textPaint)
				WatchMode.CALENDAR		-> renderCalendarView(context, canvas, bounds, textPaint)
				WatchMode.MOVEMENT		-> renderMovementView(context, canvas, bounds, textPaint)
			}

		} else if ( renderParameters.drawMode == DrawMode.AMBIENT ) {

			val dr = -90f + zonedDateTime.dayOfYear * 360f / 365f
			drawGradientArc( canvas, bounds,   0f, 200f, dr, 365f, watchFaceColors.activePrimaryColor, 64 )

		}

		// gradient
		if ( watchMode == WatchMode.WATCH ) drawGradient( canvas, currentWatchFaceSize )

		if ( renderParameters.drawMode != DrawMode.AMBIENT && watchMode == WatchMode.WATCH ) {

			// lunette overlay
			drawLunette( canvas, bounds )

			// text zones
			drawTextZones(canvas, bounds, themeName, zonedDateTime )

		}

		// overlay transition
		val paint = Paint().apply { alpha = transitionAlpha.toInt() }
		canvas.drawPaint(paint)

		// drawShaderLayer( canvas, bounds, zonedDateTime )

	}

	//
	//	shader
	//

	// @Language("AGSL")
	// private val SIMPLE = """
	// 	uniform float2 resolution;
	// 	half4 main(float2 coord) {
	// 		float2 uv = coord.xy / resolution;
	// 		// R G B A
	// 		return half4( uv.x, uv.y, 0.0, 1.0);
	// 	}
	// """.trimIndent()

	// private fun drawShaderLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime ) {

		// Log.d(TAG, "drawShaderLayer()")
		// val shaderSource = RuntimeShader( SIMPLE )
		// 	shaderSource.setFloatUniform( "resolution", bounds.width().toFloat(), bounds.height().toFloat() )
		// 	// shaderSource.setFloatUniform( "time", zonedDateTime.second.toFloat() ) //toEpochMilli
		// val shaderPaint = Paint().apply {
		// 	shader = shaderSource
		// 	alpha = 64
		// 	blendMode = BlendMode.MULTIPLY
		// }
		// canvas.drawPaint(shaderPaint)
	// }

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

	private fun drawTextZones( canvas: Canvas, bounds: Rect, themeName: String, zonedDateTime: ZonedDateTime ) {

		val xc = bounds.exactCenterX()
		val yc = bounds.exactCenterY()

		val t1 = themeName
		canvas.drawText( t1, xc, yc / 2f + 8f, p1 )

		val t2 = zonedDateTime.hour.toString() + "'" + zonedDateTime.minute.toString() + "'" + zonedDateTime.second.toString() + " LT"
		val t3 = "13'37'00 UT"
		canvas.drawText( t2, xc - 16, yc - 8f, p2 )
		canvas.drawText( t3, xc - 16, yc + 20f, p2 )

	}

	//
	//	draw lunette
	//

	private fun drawLunette(
		canvas: Canvas,
		bounds: Rect
	) {

		// if ( watchFaceData.drawHourPips ) {
			// drawNumberStyleOuterElement(
			// 	canvas,
			// 	bounds,
			// 	watchFaceData.numberRadiusFraction,
			// 	watchFaceData.numberStyleOuterCircleRadiusFraction,
			// 	watchFaceColors.activeOuterElementColor,
			// 	watchFaceData.numberStyleOuterCircleRadiusFraction,
			// 	watchFaceData.gapBetweenOuterCircleAndBorderFraction
			// 	)
		// } else {

			val offset = 20f
			val space = 10f

			val arcOffset = 1f
			val arcSweep = 90f - arcOffset - arcOffset

			val rect = RectF( offset, offset, bounds.width().toFloat() - offset, bounds.height().toFloat() - offset )
			val r01 = RectF( space, space, bounds.width().toFloat() - space, bounds.height().toFloat() - space )

			val p = Paint().apply {
				color = Color.YELLOW
				strokeWidth = 18f
				style = Paint.Style.STROKE
				typeface = typeface
				// strokeJoin = Paint.Join.ROUND
				// strokeCap = Paint.Cap.ROUND
			}

			// canvas.drawArc(r01, -90f + arcOffset ,arcSweep, false, p)
			// canvas.drawArc(r01,   0f + arcOffset, arcSweep, false, p)
			// canvas.drawArc(r01, 180f + arcOffset, arcSweep, false, p)
			// canvas.drawArc(r01,  90f + arcOffset, arcSweep, false, p)

			//

			textPaint.textSize = 16f
			textPaint.color = Color.WHITE

			val t0 = "0123456789abcdef"
			val p0 = Path()
			p0.addArc( rect, -90f + arcOffset, 90f )
			canvas.drawTextOnPath( t0, p0, 0f, 0f, textPaint )

			val t1 = "ZONE 2"
			val p1 = Path()
			p1.addArc( rect, 0f + arcOffset, 90f )
			canvas.drawTextOnPath( t1, p1, 0f, 0f, textPaint )

			val t2 = "ZONE 3"
			val p2 = Path()
			p2.addArc( rect, 90f + arcOffset, 90f )
			canvas.drawTextOnPath( t2, p2, 0f, 0f, textPaint )

			val t3 = "ZONE 4"
			val p3 = Path()
			p3.addArc( rect, 180f + arcOffset, 90f )
			canvas.drawTextOnPath( t3, p3, 0f, 0f, textPaint )

		// }

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
			val dialGap = 1f
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

    private fun drawNumberStyleOuterElement(
        canvas: Canvas,
        bounds: Rect,
        numberRadiusFraction: Float,
        outerCircleStokeWidthFraction: Float,
        outerElementColor: Int,
        numberStyleOuterCircleRadiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {

        outerElementPaint.strokeWidth = outerCircleStokeWidthFraction * bounds.width()
        outerElementPaint.color = outerElementColor
        canvas.save()

        for (i in 0 until 60) {
            // if (i % 15 != 0) {
                drawTopMiddleCircle(
                    canvas,
                    bounds,
                    numberStyleOuterCircleRadiusFraction/4,
                    gapBetweenOuterCircleAndBorderFraction
                )
            // }
            canvas.rotate(360.0f / 60.0f, bounds.exactCenterX(), bounds.exactCenterY() )
        }
        canvas.restore()

    }

    /** Draws the outer circle on the top middle of the given bounds. */
    private fun drawTopMiddleCircle(
        canvas: Canvas,
        bounds: Rect,
        radiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {

        outerElementPaint.style = Paint.Style.FILL_AND_STROKE

        val centerX = 0.5f * bounds.width().toFloat()
        val centerY = bounds.width() * (gapBetweenOuterCircleAndBorderFraction + radiusFraction)

        canvas.drawCircle(
            centerX,
            centerY,
            radiusFraction * bounds.width(),
            outerElementPaint
        )

    }

    companion object {
        private const val TAG = "CanvasRenderer"
        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")
        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f
    }
}
