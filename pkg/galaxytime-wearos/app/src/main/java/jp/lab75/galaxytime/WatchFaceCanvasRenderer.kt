/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.lab75.galaxytime

import android.content.Context
import android.content.res.Resources

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect


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
// import androidx.compose.animation.AnimatedVisibility

// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

/**
 * Renders watch face via data in Room database. Also, updates watch face state based on setting
 * changes by user via [userStyleRepository.addUserStyleListener()].
 */
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

	// scene transitions
	//
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


	// data
	//
	//
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var watchFaceData: WatchFaceData = WatchFaceData()

	// styling
	//
	//
	private var watchFaceColors = convertToWatchFaceColorPalette(
        context,
        watchFaceData.activeColorStyle,
        watchFaceData.ambientColorStyle
    )
	private val outerElementPaint = Paint().apply {
		isAntiAlias = true
	}
	// Initializes paint object for painting the clock hands with default values.
    private val clockHandPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.clock_hand_stroke_width).toFloat()
    }
    // Used to paint the main hour hand text with the hour pips, i.e., 3, 6, 9, and 12 o'clock.
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = context.resources.getDimensionPixelSize(R.dimen.hour_mark_size).toFloat()
    }

	// images
	//
	//
	private lateinit var ssImage: Bitmap
	private lateinit var mmImage: Bitmap
	private lateinit var hhImage: Bitmap
	private lateinit var ddImage: Bitmap
	private lateinit var gradientImage: Bitmap

    // Changed when setting changes cause a change in the minute hand arm (triggered by user in
    // updateUserStyle() via userStyleRepository.addUserStyleListener()).
    private var armLengthChangedRecalculateClockHands: Boolean = false

    // Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with
    // valid dimensions from the system.
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

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

    /*
     * Triggered when the user makes changes to the watch face through the settings activity. The
     * function is called by a flow.
     */

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
                    // val doubleValue = options.value as
                        // UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

                    // The arm lengths are usually only calculated the first time the watch face is
                    // loaded to reduce the ops in the onDraw(). Because we updated the minute hand
                    // watch length, we need to trigger a recalculation.
                    // armLengthChangedRecalculateClockHands = true

                    // Updates length of minute hand based on edits from user.
                    // val newMinuteHandDimensions = newWatchFaceData.minuteHandDimensions.copy(
                    //     lengthFraction = doubleValue.value.toFloat()
                    // )

                    // newWatchFaceData = newWatchFaceData.copy(
                    //     minuteHandDimensions = newMinuteHandDimensions
                    // )
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

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        scope.cancel("GalaxyWatchCanvasRenderer scope clear() request")
        super.onDestroy()
    }

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



    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: AnalogSharedAssets
    ) {

		val resources: Resources = context.resources
		val gradientBitmap = BitmapFactory.decodeResource(resources, R.drawable.outergradient)
		gradientImage = Bitmap.createScaledBitmap(gradientBitmap, bounds.width(), bounds.height(), false)

		// background color
		val backgroundColor = if ( renderParameters.drawMode == DrawMode.AMBIENT ) {
			watchFaceColors.ambientBackgroundColor
        } else {
			watchFaceColors.activeBackgroundColor
        }
        canvas.drawColor( backgroundColor )

		// watch face states
		if ( watchMode == WatchMode.WATCH ) renderWatchView(canvas, bounds, zonedDateTime)
		if ( watchMode == WatchMode.BIOMETRICS ) renderBiometricsView(canvas, bounds)
		if ( watchMode == WatchMode.ASTRONOMICS ) renderAstronomicsView(canvas, bounds, zonedDateTime)

		// gradient
		canvas.drawBitmap( gradientImage, currentWatchFaceSize, currentWatchFaceSize, null )

		// transition
		val paint = Paint().apply { alpha = transitionAlpha.toInt() }
		canvas.drawPaint(paint)

	}

	private fun renderBiometricsView( canvas: Canvas, bounds: Rect	) {

		val style = Paint().apply {
			isAntiAlias = true
			style = Paint.Style.FILL_AND_STROKE
		}
		val centerX = 0.5f * bounds.width().toFloat()
		val centerY = 0.5f * bounds.height().toFloat()
		val radius = 0.5f * bounds.width()

		canvas.drawCircle(
			centerX,
			centerY,
			radius,
			style
		)

		val textBounds = Rect()
        textPaint.color = watchFaceColors.activeOuterElementColor
		textPaint.getTextBounds( "hello", 0, 5, textBounds )

		canvas.drawText(
			"hello",
			bounds.exactCenterX() - textBounds.width() / 2,
			bounds.exactCenterY() - textBounds.height() / 2,
			textPaint
		)

	}

	private fun renderAstronomicsView(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {

	}

	private fun renderWatchView(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {

		val resources: Resources = context.resources

		val ssBitmap = BitmapFactory.decodeResource(resources, R.drawable.ss)
		ssImage = Bitmap.createScaledBitmap(ssBitmap, bounds.width(), bounds.height(), true)
		val mmBitmap = BitmapFactory.decodeResource(resources, R.drawable.mm)
		mmImage = Bitmap.createScaledBitmap(mmBitmap, bounds.width(), bounds.height(), true)
		val hhBitmap = BitmapFactory.decodeResource(resources, R.drawable.hh)
		hhImage = Bitmap.createScaledBitmap(hhBitmap, bounds.width(), bounds.height(), true)
		val ddBitmap = BitmapFactory.decodeResource(resources, R.drawable.dd)
		ddImage = Bitmap.createScaledBitmap(ddBitmap, bounds.width(), bounds.height(), true)



        // CanvasComplicationDrawable already obeys rendererParameters.
        // if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)) {
		// 	drawComplications(canvas, zonedDateTime)
		// }

		drawClockHands(canvas, bounds, zonedDateTime)

		// if pips are enabled, draw them
        if (renderParameters.drawMode == DrawMode.INTERACTIVE &&
            renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE) &&
            watchFaceData.drawHourPips
        ) {
            drawNumberStyleOuterElement(
                canvas,
                bounds,
                watchFaceData.numberRadiusFraction,
                watchFaceData.numberStyleOuterCircleRadiusFraction,
                watchFaceColors.activeOuterElementColor,
                watchFaceData.numberStyleOuterCircleRadiusFraction,
                watchFaceData.gapBetweenOuterCircleAndBorderFraction
            )
        }
    }


	// drawing functions

    private fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.render(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    private fun drawClockHands(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime
    ) {
        // Only recalculate bounds (watch face size/surface) has changed or
		// the arm of one of the clock hands has changed (via user input in the settings).
        // NOTE: Watch face surface usually only updates one time
		// (when the size of the device is initially broadcasted).
        if (currentWatchFaceSize != bounds || armLengthChangedRecalculateClockHands) {
            currentWatchFaceSize = bounds
        }

        val secondOfDay = zonedDateTime.toLocalTime().toSecondOfDay()
        val secondsPerHourHandRotation = Duration.ofHours(12).seconds
        val secondsPerMinuteHandRotation = Duration.ofHours(1).seconds

		val sRot = secondOfDay.rem( secondsPerMinuteHandRotation ) * 1f
		val mRot = secondOfDay.rem( secondsPerMinuteHandRotation ) * 360.0f / secondsPerMinuteHandRotation
        val hRot = secondOfDay.rem( secondsPerHourHandRotation ) * 360.0f / secondsPerHourHandRotation

        canvas.withScale(
            x = WATCH_HAND_SCALE,
            y = WATCH_HAND_SCALE,
            pivotX = bounds.exactCenterX(),
            pivotY = bounds.exactCenterY()
        ) {
            val drawAmbient = renderParameters.drawMode == DrawMode.AMBIENT

			// use this to color the dials:::
			clockHandPaint.color = if (drawAmbient) {
                watchFaceColors.ambientPrimaryColor
            } else {
                watchFaceColors.activePrimaryColor
            }

            // Draw all the stuff when not in ambient mode
            if (!drawAmbient) {

				val m1 = Matrix()
				m1.postRotate( sRot, ssImage.width / 2f, ssImage.height / 2f )
				canvas.drawBitmap( ssImage, m1, null )

				val m2 = Matrix()
				m2.postRotate( mRot, mmImage.width / 2f, mmImage.height / 2f )
				canvas.drawBitmap( mmImage, m2, null )

				val m3 = Matrix()
				m3.postRotate( hRot, hhImage.width / 2f, hhImage.height / 2f )
				canvas.drawBitmap( hhImage, m3, null )

				// matrix.postRotate( 17f, ddImage.width / 2f, ddImage.height / 2f)
				// canvas.drawBitmap(ddImage, matrix, null)
			}
        }
    }


    /*
     * Rarely called (only when watch face surface changes; usually only once) from the
     * drawClockHands() method.
     */
    // private fun recalculateClockHands(bounds: Rect) {
    //     Log.d(TAG, "recalculateClockHands()")
    //     hourHandBorder =
    //         createClockHand(
    //             bounds,
    //             watchFaceData.hourHandDimensions.lengthFraction,
    //             watchFaceData.hourHandDimensions.widthFraction,
    //             watchFaceData.gapBetweenHandAndCenterFraction,
    //             watchFaceData.hourHandDimensions.xRadiusRoundedCorners,
    //             watchFaceData.hourHandDimensions.yRadiusRoundedCorners
    //         )
    //     hourHandFill = hourHandBorder

    //     minuteHandBorder =
    //         createClockHand(
    //             bounds,
    //             watchFaceData.minuteHandDimensions.lengthFraction,
    //             watchFaceData.minuteHandDimensions.widthFraction,
    //             watchFaceData.gapBetweenHandAndCenterFraction,
    //             watchFaceData.minuteHandDimensions.xRadiusRoundedCorners,
    //             watchFaceData.minuteHandDimensions.yRadiusRoundedCorners
    //         )
    //     minuteHandFill = minuteHandBorder

    //     secondHand =
    //         createClockHand(
    //             bounds,
    //             watchFaceData.secondHandDimensions.lengthFraction,
    //             watchFaceData.secondHandDimensions.widthFraction,
    //             watchFaceData.gapBetweenHandAndCenterFraction,
    //             watchFaceData.secondHandDimensions.xRadiusRoundedCorners,
    //             watchFaceData.secondHandDimensions.yRadiusRoundedCorners
    //         )
    // }

    /**
     * Returns a round rect clock hand if {@code rx} and {@code ry} equals to 0, otherwise return a
     * rect clock hand.
     *
     * @param bounds The bounds use to determine the coordinate of the clock hand.
     * @param length Clock hand's length, in fraction of {@code bounds.width()}.
     * @param thickness Clock hand's thickness, in fraction of {@code bounds.width()}.
     * @param gapBetweenHandAndCenter Gap between inner side of arm and center.
     * @param roundedCornerXRadius The x-radius of the rounded corners on the round-rectangle.
     * @param roundedCornerYRadius The y-radius of the rounded corners on the round-rectangle.
     */
    // private fun createClockHand(
    //     bounds: Rect,
    //     length: Float,
    //     thickness: Float,
    //     gapBetweenHandAndCenter: Float,
    //     roundedCornerXRadius: Float,
    //     roundedCornerYRadius: Float
    // ): Path {
    //     val width = bounds.width()
    //     val centerX = bounds.exactCenterX()
    //     val centerY = bounds.exactCenterY()
    //     val left = centerX - thickness / 2 * width
    //     val top = centerY - (gapBetweenHandAndCenter + length) * width
    //     val right = centerX + thickness / 2 * width
    //     val bottom = centerY - gapBetweenHandAndCenter * width
    //     val path = Path()

    //     if (roundedCornerXRadius != 0.0f || roundedCornerYRadius != 0.0f) {
    //         path.addRoundRect(
    //             left,
    //             top,
    //             right,
    //             bottom,
    //             roundedCornerXRadius,
    //             roundedCornerYRadius,
    //             Path.Direction.CW
    //         )
    //     } else {
    //         path.addRect(
    //             left,
    //             top,
    //             right,
    //             bottom,
    //             Path.Direction.CW
    //         )
    //     }
    //     return path
    // }

    private fun drawNumberStyleOuterElement(
        canvas: Canvas,
        bounds: Rect,
        numberRadiusFraction: Float,
        outerCircleStokeWidthFraction: Float,
        outerElementColor: Int,
        numberStyleOuterCircleRadiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {
        // Draws text hour indicators (12, 3, 6, and 9).
        // val textBounds = Rect()
        // textPaint.color = outerElementColor
        // for (i in 0 until 4) {
        //     val rotation = 0.5f * (i + 1).toFloat() * Math.PI
        //     val dx = sin(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
        //     val dy = -cos(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
        //     textPaint.getTextBounds(HOUR_MARKS[i], 0, HOUR_MARKS[i].length, textBounds)
        //     canvas.drawText(
        //         HOUR_MARKS[i],
        //         bounds.exactCenterX() + dx - textBounds.width() / 2.0f,
        //         bounds.exactCenterY() + dy + textBounds.height() / 2.0f,
        //         textPaint
        //     )
        // }

        // Draws dots for the remain hour indicators between the numbers above.
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

        // X and Y coordinates of the center of the circle.
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
        private const val TAG = "WatchCanvasRenderer"

        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")

        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f
    }
}
