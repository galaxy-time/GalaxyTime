package jp.lab75.galaxytime

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

import android.util.Log
import android.view.SurfaceHolder
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale

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


import java.time.Duration
import java.time.ZonedDateTime
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

	public var watchMode: WatchMode = WatchMode.WATCH
	public var nextWatchMode: WatchMode = WatchMode.WATCH

	private enum class TransitionMode { IN, OUT, IDLE }
	private var transitionMode: TransitionMode = TransitionMode.IDLE
	private var prevMode: TransitionMode = TransitionMode.IDLE

	private var transitionAlpha = 0f

	override fun onTapEvent( tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot? ) {
		if ( tapType == TapType.UP ) {
			nextWatchMode = when ( watchMode ) {
				WatchMode.WATCH -> WatchMode.BIOMETRICS
				WatchMode.BIOMETRICS -> WatchMode.ASTRONOMICS
				WatchMode.ASTRONOMICS -> WatchMode.WATCH
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
    private val textPaint = Paint().apply {
        isAntiAlias = true
		color = Color.WHITE
		// style = Paint.Style.FILL
		typeface = typeface
        textSize = context.resources.getDimensionPixelSize(R.dimen.settings_default_text_size).toFloat()
    }
    private var armLengthChangedRecalculateClockHands: Boolean = false
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

	//
	// init image containers
	//

	private lateinit var ssImage: Bitmap
	private lateinit var mmImage: Bitmap
	private lateinit var hhImage: Bitmap
	private lateinit var ddImage: Bitmap
	private lateinit var gradientImage: Bitmap

	val ssBitmap = BitmapFactory.decodeResource(resources, R.drawable.ss)
	val mmBitmap = BitmapFactory.decodeResource(resources, R.drawable.mm)
	val hhBitmap = BitmapFactory.decodeResource(resources, R.drawable.hh)
	val ddBitmap = BitmapFactory.decodeResource(resources, R.drawable.dd)
	val gradientBitmap = BitmapFactory.decodeResource(resources, R.drawable.outergradient)

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
	//	update graphics
	//

	private fun updateGraphics() {
		ssImage = Bitmap.createScaledBitmap(ssBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false)
		mmImage = Bitmap.createScaledBitmap(mmBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false)
		hhImage = Bitmap.createScaledBitmap(hhBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false)
		ddImage = Bitmap.createScaledBitmap(ddBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false)
		gradientImage = Bitmap.createScaledBitmap(gradientBitmap, currentWatchFaceSize.width(), currentWatchFaceSize.height(), false)
	}

	//
	//	update theme
	//

    private fun updateWatchFaceData(userStyle: UserStyle) {

        Log.d(TAG, "updateWatchFace(): $userStyle")
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

		if ( currentWatchFaceSize != bounds ) {
            currentWatchFaceSize = bounds
            updateGraphics()
        }

		// val gradientBitmap = BitmapFactory.decodeResource(resources, R.drawable.outergradient)
		// gradientImage = Bitmap.createScaledBitmap(gradientBitmap, bounds.width(), bounds.height(), false)

		// val filter = PorterDuffColorFilter( context.getColor( null, watchFaceColors.activeBackgroundColor ), PorterDuff.Mode.SRC_IN);
		// val paintOverlay = Paint()
		// paintOverlay.setColorFilter(filter)

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
		canvas.drawBitmap( gradientImage, currentWatchFaceSize, currentWatchFaceSize, null )

		// lunette overlay...
		if ( watchMode == WatchMode.WATCH ) drawLunette( canvas, bounds )

		// overlay transition
		val paint = Paint().apply { alpha = transitionAlpha.toInt() }
		canvas.drawPaint(paint)
	}

	//
	//	watch view
	//	TODO: move to views
	//

	private fun renderWatchView( context: Context, canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime ) {

		// complications

        // if ( watchFaceData.drawComplications &&
		// 	renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY) ) {
		// 	drawComplications(canvas, zonedDateTime)
		// }

		// hands

		drawClockHands(canvas, bounds, zonedDateTime)

    }

	//
	//	draw lunette
	//

	private fun drawLunette(
		canvas: Canvas,
		bounds: Rect
	) {

		if ( watchFaceData.drawHourPips ) {
			drawNumberStyleOuterElement(
				canvas,
				bounds,
				watchFaceData.numberRadiusFraction,
				watchFaceData.numberStyleOuterCircleRadiusFraction,
				watchFaceColors.activeOuterElementColor,
				watchFaceData.numberStyleOuterCircleRadiusFraction,
				watchFaceData.gapBetweenOuterCircleAndBorderFraction
				)
		} else {
			textPaint.textSize = 10f
			val text = "The Quick Brown Fox Jumped Over The Lazy Dog"
			val off = 20f
			val rect = RectF( off, off, bounds.width().toFloat() - off - off, bounds.height().toFloat() - off - off )
			val path = Path()
			path.addArc( rect, -180f, 180f )
			canvas.drawTextOnPath( text, path, 0f, 0f, textPaint )
		}

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
        // Only recalculate bounds (watch face size/surface) has changed or
		// the arm of one of the clock hands has changed (via user input in the settings).
        // NOTE: Watch face surface usually only updates one time
		// (when the size of the device is initially broadcasted).
        // if (currentWatchFaceSize != bounds || armLengthChangedRecalculateClockHands) {
        //     currentWatchFaceSize = bounds
        // }

        val secondOfDay = zonedDateTime.toLocalTime().toSecondOfDay()
        val secondsPerHourHandRotation = Duration.ofHours(12).seconds
        val secondsPerMinuteHandRotation = Duration.ofHours(1).seconds

		// TODO: get day in seconds from api
		// e.g. one rotation == one day == 100 hours == 100 * 60 * 60 seconds
		val secondsPerDayHandRotation = Duration.ofHours(100).seconds

		val sRot = secondOfDay.rem( secondsPerMinuteHandRotation ) * 1f
		val mRot = secondOfDay.rem( secondsPerMinuteHandRotation ) * 360.0f / secondsPerMinuteHandRotation
        val hRot = secondOfDay.rem( secondsPerHourHandRotation ) * 360.0f / secondsPerHourHandRotation
		val dRot = secondOfDay.rem( secondsPerHourHandRotation ) * 360.0f / secondsPerDayHandRotation

		if (
			renderParameters.drawMode == DrawMode.INTERACTIVE &&
			renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE)
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

			val m1 = Matrix()
			m1.postRotate( sRot, ssImage.width / 2f, ssImage.height / 2f )
			canvas.drawBitmap( ssImage, m1, null )

			val m2 = Matrix()
			m2.postRotate( mRot, mmImage.width / 2f, mmImage.height / 2f )
			canvas.drawBitmap( mmImage, m2, null )

			val m3 = Matrix()
			m3.postRotate( hRot, hhImage.width / 2f, hhImage.height / 2f )
			canvas.drawBitmap( hhImage, m3, null )

			val m4 = Matrix()
			m4.postRotate( dRot, ddImage.width / 2f, ddImage.height / 2f)
			canvas.drawBitmap(ddImage, m4, null)

		} else if ( renderParameters.drawMode == DrawMode.AMBIENT ) {

			val m4 = Matrix()
			m4.postRotate( dRot, ddImage.width / 2f, ddImage.height / 2f)
			canvas.drawBitmap(ddImage, m4, null)

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
