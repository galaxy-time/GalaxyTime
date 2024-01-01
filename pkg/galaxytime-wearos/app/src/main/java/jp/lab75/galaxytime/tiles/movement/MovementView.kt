//
// movement view
//
//
//
package jp.lab75.galaxytime

import android.content.Context
import android.content.res.Resources

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Color

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

import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private lateinit var gradientImage: Bitmap

 fun renderMovementView(
	context: Context,
	canvas: Canvas,
	bounds: Rect,
	defaultStyle: Paint,
) {

	var watchFaceData: WatchFaceData = WatchFaceData()
	var watchFaceColors = convertToWatchFaceColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	val style = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL_AND_STROKE
        textSize = 20f
	}

	val centerX = bounds.exactCenterX().toFloat()
	val centerY = bounds.exactCenterY().toFloat()

	// val radius = 0.5f * bounds.width()
	// canvas.drawCircle(
	// 	centerX,
	// 	centerY,
	// 	radius,
	// 	style
	// )

	val str = "MOVEMENT"
	val textBounds = Rect()
	defaultStyle.color = watchFaceColors.activeOuterElementColor
	defaultStyle.textSize = 20f
	defaultStyle.getTextBounds( str, 0, 5, textBounds )
	defaultStyle.textAlign = Paint.Align.CENTER

	canvas.drawText(
		str,
		centerX,
		centerY,
		defaultStyle
	)
}


