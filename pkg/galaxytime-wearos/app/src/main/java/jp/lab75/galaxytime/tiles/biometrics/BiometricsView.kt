//
// 	biometrics view shows hydration rate and heart rate
//
//
//

package jp.lab75.galaxytime

import android.content.Context
import android.content.res.Resources

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Color

import android.graphics.Bitmap
import android.graphics.BitmapFactory

import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData

private lateinit var backgroundImage: Bitmap
private val padding = 25f
private val typePadding = 20f
private val size = 20f
private val fontSize = 20f

// draw a line of text vertically aligned at the given position

fun addText( x: Float, y: Float, string: String, style: Paint, canvas: Canvas ) {
	val str = string
	val bounds = Rect()
	style.getTextBounds( str, 0, str.length, bounds )
	canvas.drawText( str, x, y - bounds.exactCenterY(), style )
}

// draw a vertical range of steps from start to end

fun addRange( x:Float, y: Float, height: Float, steps: Int, start: Int, end: Int, style: Paint, canvas: Canvas ) {
	for ( i in 0..steps ) {
		val c = -i
		addText(
			x,
			y + i * height / steps.toFloat(),
			"$c",
			style,
			canvas
		)
	}
}

//
//
//

fun renderBiometricsView(
	context: Context,
	canvas: Canvas,
	bounds: Rect,
	textStyle: Paint,
) {

	val resources: Resources = context.resources

	val watchFaceData: WatchFaceData = WatchFaceData()
	val watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	// background

	val backgroundBitmap = BitmapFactory.decodeResource( resources, R.drawable.sgt_hydration )
	backgroundImage = Bitmap.createScaledBitmap( backgroundBitmap, bounds.width(), bounds.height(), false )

	canvas.drawBitmap( backgroundImage, bounds, bounds, null )

	// view

	val lineStyle = Paint().apply {
        isAntiAlias = true
		strokeWidth = 1f
		style = Paint.Style.STROKE
		color = Color.WHITE
    }

	val centerX = 0.5f * bounds.width().toFloat()
	val centerY = 0.5f * bounds.height().toFloat()

	canvas.drawLine(
		padding + 5,
		centerY,
		bounds.width().toFloat() - padding -  5,
		centerY,
		lineStyle
	)

	canvas.drawLine(
		centerX,
		padding + size + 5,
		centerX,
		bounds.height().toFloat() - padding - size - 5,
		lineStyle
	)

	// canvas.drawCircle(
	// 	centerX,
	// 	centerY,
	// 	radius,
	// 	style
	// )

	textStyle.color = watchFaceColors.activeOuterElementColor
	textStyle.textSize = fontSize
	textStyle.textAlign = Paint.Align.CENTER
	addText( centerX, padding, "HIGH", textStyle, canvas )
	addText( centerX, bounds.height() - padding, "LOW", textStyle, canvas )

	textStyle.textAlign = Paint.Align.RIGHT
	addText( centerX - 20f, centerY + 30f, "HYDRATION HRS", textStyle, canvas )
	addText( centerX - 20f, centerY + 70f, "TAP TO CONFIRM", textStyle, canvas )
	addText( centerX - 20f, centerY + 90f, "REHYDRATION", textStyle, canvas )

	val height = bounds.height().toFloat() - padding - padding - size - size - 20
	addRange( centerX + 40f, padding + size + 10, height, 9, 0, 100 , textStyle, canvas )

	// val strHi = "HIGH"
	// val textBoundsHi = Rect()
	// textStyle.getTextBounds( strHi, 0, strHi.length, textBoundsHi)

	// canvas.drawText(
	// 	strHi,
	// 	centerX,
	// 	padding - textBoundsHi.exactCenterY(),
	// 	textStyle
	// )

	// val strLo = "LOW"
	// val textBoundsLo = Rect()
	// textStyle.getTextBounds( strLo, 0, strLo.length, textBoundsLo)

	// canvas.drawText(
	// 	strLo,
	// 	centerX,
	// 	bounds.height() - padding - textBoundsLo.exactCenterY(),
	// 	textStyle
	// )

	// val items: String[] = [ "0", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8" ]
	// textStyle.textAlign = Paint.Align.RIGHT

	// for (item in items) {
	// 	val rect = Rect()
	// 	textStyle.getTextBounds( item, 0, item.length, rect )
	// 	canvas.drawText(
	// 		item,
	// 		centerX,
	// 		bounds.height() - padding - rect.exactCenterY(),
	// 		textStyle
	// 	)
	// }

}


