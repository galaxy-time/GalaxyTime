//
// 	astronomics view shows distances from earth
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
import android.graphics.BlendMode

import io.github.cosinekitty.astronomy.Body

import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData

import jp.lab75.galaxytime.service.Calculations
import kotlin.math.roundToInt

private lateinit var backgroundImage: Bitmap

private val padding = 25f
private val typePadding = 20f
private val size = 20f
private val fontSize = 16f

//
//
//

fun renderAstronomicsView(
	context: Context,
	canvas: Canvas,
	bounds: Rect,
	textStyle: Paint,
	calculations: Calculations
) {

	val resources: Resources = context.resources

	var watchFaceData: WatchFaceData = WatchFaceData()
	var watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	// val name = watchFaceData.activeColorStyle.toString()

	val earthData = calculations.getBodyData(Body.Earth)

	val l1 = "AZI ${
		earthData?.horizontal?.azimuth?.roundToInt()?.or(272)
	}° ELE ${earthData?.horizontal?.altitude?.roundToInt()?.or(-2)}°"

	// Create time with padding for this
	val timeFormat = "RAS %02dh:%02dm:%02ds"
	val l2 = timeFormat.format(
		earthData?.rightAscension?.degrees?.or(12),
		earthData?.rightAscension?.minutes?.or(14),
		earthData?.rightAscension?.seconds?.roundToInt()?.or(16)
	)


	//val l1 = "AZI 272° ELE -2°"
	//val l2 = "RAS 12h14m16s"
	val l3 = "DEC +00°36'26''"
	val l4 = ""

	val r1 = "24h 37min"
	val r2 = "687 EARTH DAYS"
	val r3 = "1.63118 × 1011 km³"
	val r4 = "SATELLITES · ·"

	// background

	val backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.sgt_planet_neutral)
	backgroundImage =
		Bitmap.createScaledBitmap(backgroundBitmap, bounds.width(), bounds.height(), false)
	canvas.drawBitmap(backgroundImage, bounds, bounds, null)

	val overlayColor = watchFaceColors.activePrimaryColor
	canvas.drawColor(overlayColor, BlendMode.OVERLAY)

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
		115f,
		centerY - 20f,
		bounds.width().toFloat() - 105f,
		centerY - 20f,
		lineStyle
	)

	canvas.drawCircle(
		centerX - 140,
		centerY - 20f,
		30f,
		lineStyle
	)
	canvas.drawCircle(
		centerX + 140,
		centerY - 20f,
		20f,
		lineStyle
	)

	textStyle.color = watchFaceColors.activeOuterElementColor
	textStyle.textSize = fontSize
	textStyle.textAlign = Paint.Align.CENTER

	val destination = "EARTH"
	val location = watchFaceData.activeColorStyle.toString()
	addText(centerX, centerY - 140f, "$location — $destination", textStyle, canvas)

	val xoff_left = 160f
	val xoff_right = 20f

	textStyle.textAlign = Paint.Align.LEFT

	addText(centerX - xoff_left, centerY + 30f, l1, textStyle, canvas)
	addText(centerX - xoff_left, centerY + 50f, l2, textStyle, canvas)
	addText(centerX - xoff_left, centerY + 70f, l3, textStyle, canvas)

	addText(centerX + xoff_right, centerY + 30f, r1, textStyle, canvas)
	addText(centerX + xoff_right, centerY + 50f, r2, textStyle, canvas)
	addText(centerX + xoff_right, centerY + 70f, r3, textStyle, canvas)
	addText(centerX + xoff_right, centerY + 90f, r4, textStyle, canvas)

	// val height = bounds.height().toFloat() - padding - padding - size - size - 20
	// addRange( centerX + 40f, padding + size + 10, height, 9, 0, 100 , textStyle, canvas )

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


