//
// 	calendar view shows meetings and events
//
//
//
package jp.lab75.galaxytime

import android.content.Context

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

import android.graphics.Bitmap

import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData

private lateinit var gradientImage: Bitmap

 fun renderCalendarView(
	context: Context,
	canvas: Canvas,
	bounds: Rect,
	defaultStyle: Paint,
) {

	var watchFaceData: WatchFaceData = WatchFaceData()
	var watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	val style = Paint().apply {
		isAntiAlias = true
		style = Paint.Style.FILL_AND_STROKE
        textSize = 20f
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

	val str = "CALENDAR"
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


