package jp.lab75.galaxytime
import org.intellij.lang.annotations.Language

import android.content.Context
import android.content.res.Resources

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Color
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuff
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.BlendMode

import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale

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
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import jp.lab75.galaxytime.data.watchface.WatchMode

import jp.lab75.galaxytime.utils.COLOR_STYLE_SETTING
import jp.lab75.galaxytime.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import jp.lab75.galaxytime.utils.WATCH_HAND_LENGTH_STYLE_SETTING

import jp.lab75.galaxytime.renderWatchfaceView
import jp.lab75.galaxytime.renderBiometricsView
import jp.lab75.galaxytime.renderAstronomicsView
import jp.lab75.galaxytime.primitives.drawGradientArc

import java.time.Duration
import java.time.ZonedDateTime
import java.time.Year

import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter

//	shaders
import android.graphics.RuntimeShader

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
	private var transitionMode: TransitionMode = TransitionMode.IDLE
	private var prevMode: TransitionMode = TransitionMode.IDLE

	private var transitionAlpha = 0f

	override fun onTapEvent( tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot? ) {
		// TODO: add touch segments to trigger different scenes

		Log.d(TAG, "$tapEvent.x, $tapEvent.y")

		// this only toggles through all available views.
		// remove when todo above is resolved.
		if ( tapType == TapType.UP ) {
			nextWatchMode = when ( watchMode ) {
				WatchMode.WATCH -> WatchMode.BIOMETRICS
				WatchMode.BIOMETRICS -> WatchMode.ASTRONOMICS
				WatchMode.ASTRONOMICS -> WatchMode.CALENDAR
				WatchMode.CALENDAR -> WatchMode.MOVEMENT
				WatchMode.MOVEMENT -> WatchMode.WATCH
			}
			if ( prevMode == TransitionMode.IDLE && nextWatchMode != watchMode ) fadeOut()
			Log.d(TAG, "next mode $nextWatchMode")
		}
		invalidate()
	}

	fun fadeOut() {

		Log.d(TAG, "FADEOUT $watchMode")
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
				Log.d(TAG, "FADEOUT complete")
				fadeIn()
                invalidate()
            }
        })
        animator.start()
	}

	fun fadeIn() {

		Log.d(TAG, "FADEIN $watchMode")
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
				Log.d(TAG, "FADEIN complete")
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

	private var watchFaceColors = convertToWatchFaceColorPalette(
        context,
        watchFaceData.activeColorStyle,
        watchFaceData.ambientColorStyle
    )

	private val outerElementPaint = Paint().apply {
		isAntiAlias = true
	}

	private val clockHandPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.clock_hand_stroke_width).toFloat()
    }

	private val typeface = resources.getFont(R.font.pp_stellar)
	// general text
    private val textPaint = Paint().apply {
        isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
        textSize = context.resources.getDimensionPixelSize(R.dimen.settings_default_text_size).toFloat()
		letterSpacing = 0.1f
    }

	// location zone top
	private var p1 = Paint().apply {
        isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
        textSize = 14f //context.resources.getDimensionPixelSize(R.dimen.settings_default_text_size).toFloat()
		textAlign = Paint.Align.CENTER
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f

    }

	// time zones left
	var p2 = Paint().apply {
		isAntiAlias = true
		color = Color.WHITE
		typeface = typeface
        textSize = 12f
		textAlign = Paint.Align.RIGHT
		isLinearText = true
		isSubpixelText = true
		letterSpacing = 0.1f
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

                WATCH_HAND_LENGTH_STYLE_SETTING -> {
                }

            }
        }

        // Only updates if something changed.
        if (watchFaceData != newWatchFaceData) {
            watchFaceData = newWatchFaceData

            // Recreates Color and ComplicationDrawable from resource ids.
            watchFaceColors = convertToWatchFaceColorPalette(
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

		val themeName = watchFaceData.activeColorStyle.toString()

		// background color
		val backgroundColor = if ( renderParameters.drawMode == DrawMode.AMBIENT ) {
			watchFaceColors.ambientBackgroundColor
        } else {
			watchFaceColors.activeBackgroundColor
        }
        canvas.drawColor( backgroundColor )

		// watchface states
		if ( watchMode == WatchMode.WATCH ) renderWatchView(context, canvas, bounds, zonedDateTime)
		if ( watchMode == WatchMode.BIOMETRICS ) renderBiometricsView(context, canvas, bounds, textPaint)
		if ( watchMode == WatchMode.ASTRONOMICS ) renderAstronomicsView(context, canvas, bounds)

		// gradient
		drawGradient( canvas, currentWatchFaceSize )

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

	private fun drawShaderLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime ) {

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
        // if ( watchFaceData.drawComplications &&
		// 	renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY) ) {
		// 	drawComplications(canvas, zonedDateTime)
		// }

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
		// outerElementPaint.color = Color.WHITE
		// canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), bounds.exactCenterX(), outerElementPaint )

		// draw a segment in the center

	}

	//
	//	draw text
	//

	private fun drawTextZones( canvas: Canvas, bounds: Rect, themeName: String, zonedDateTime: ZonedDateTime ) {

		val xc = bounds.width().toFloat() / 2f

		val t1 = themeName
		canvas.drawText( t1, xc, 140f, p1 )

		val t2 = zonedDateTime.hour.toString() + "'" + zonedDateTime.minute.toString() + "'" + zonedDateTime.second.toString()
		val t3 = "1'23'45'6789"
		canvas.drawText( t2, xc - 40, 220f, p2 )
		canvas.drawText( t3, xc - 40, 244f, p2 )

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
			val rect = RectF( offset, offset, bounds.width().toFloat() - offset, bounds.height().toFloat() - offset )
			textPaint.textSize = 10f

			val t1 = "The Quick Brown Fox Jumped Over The Lazy Dog"
			val p1 = Path()
			p1.addArc( rect, -180f, 180f )
			canvas.drawTextOnPath( t1, p1, 0f, 0f, textPaint )

			val t2 = "1234.5678.90AB.CDEF"
			val p2 = Path()
			p2.addArc( rect, 0f, 180f )
			canvas.drawTextOnPath( t2, p2, 0f, 0f, textPaint )


		// }

	}

	//
	//	draw complications
	//

    // private fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {
    //     for ((_, complication) in complicationSlotsManager.complicationSlots) {
    //         if (complication.enabled) {
    //             complication.render(canvas, zonedDateTime, renderParameters)
    //         }
    //     }
    // }

	//
	//	draw clock hands
	//

    private fun drawClockHands(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime
    ) {
        // Only recalculate bounds (watch face size/surface) has changed or
		// the arm of one of the clock hands has changed (via user input in the settings).
        // NOTE: Watch face surface usually only updates one time
		// (when the size of the device is initially broadcasted).
        // if (currentWatchFaceSize != bounds || armLengthChangedRecalculateClockHands) {
        //     currentWatchFaceSize = bounds
        // }

        // val secondOfDay = zonedDateTime.toLocalTime().toSecondOfDay()
        // val secondsPerHourHandRotation = Duration.ofHours(12).seconds
        // val secondsPerMinuteHandRotation = Duration.ofHours(1).seconds

		// TODO: get day in seconds from api
		// e.g. one rotation == one day == 100 hours == 100 * 60 * 60 seconds
		// val secondsPerDayHandRotation = Duration.ofHours(100).seconds

		// val sRot = secondOfDay.rem( secondsPerMinuteHandRotation ) * 1f
		// val mRot = secondOfDay.rem( secondsPerMinuteHandRotation ) * 60f / secondsPerMinuteHandRotation
        // val hRot = secondOfDay.rem( secondsPerHourHandRotation ) * 360f / secondsPerHourHandRotation
		// val dRot = secondOfDay.rem( secondsPerHourHandRotation ) * 360f / secondsPerDayHandRotation

		val sr = -90f + 6f * zonedDateTime.second
		val mr = -90f + 6f * zonedDateTime.minute
		val hr = -90f + 15f * zonedDateTime.hour // 24h dial = 15f, 12h dial= 30f
		// TODO: adopt to local solar year length of location
		val dr = -90f + zonedDateTime.dayOfYear * 360f / 365f

		// Log.d(TAG, "drawClockHands() $sr $mr $hr $dr ")

		if (
			renderParameters.drawMode == DrawMode.INTERACTIVE
			// && renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE)
		) {

        // canvas.withScale(
        //     x = WATCH_HAND_SCALE,
        //     y = WATCH_HAND_SCALE,
        //     pivotX = bounds.exactCenterX(),
        //     pivotY = bounds.exactCenterY()
        // ) {
            // val drawAmbient = renderParameters.drawMode == DrawMode.AMBIENT

			// color the dials:::
			// clockHandPaint.color = if (drawAmbient) {
            //     watchFaceColors.ambientPrimaryColor
            // } else {
            //     watchFaceColors.activePrimaryColor
            // }

            // Draw all the stuff when not in ambient mode
            // if ( !drawAmbient ) {

			// activePrimaryColor=-1138278, activeSecondaryColor=-2131844710, activeBackgroundColor=-14606819, activeOuterElementColor=1307484570,
			// ambientPrimaryColor=-1, ambientSecondaryColor=-2130706433, ambientBackgroundColor=869425216, ambientOuterElementColor=1308622847

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

		} else if ( renderParameters.drawMode == DrawMode.AMBIENT ) {
			drawGradientArc( canvas, bounds,   0f, 200f, dr, 365f, watchFaceColors.activePrimaryColor, 64 )
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
