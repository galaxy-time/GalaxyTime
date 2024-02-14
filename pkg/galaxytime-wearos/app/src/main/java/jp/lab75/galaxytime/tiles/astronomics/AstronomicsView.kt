//
// 	astronomics view shows distances from earth
//
//
//

package jp.lab75.galaxytime

import android.util.Log
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

import jp.lab75.galaxytime.WatchFaceCanvasRenderer
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import jp.lab75.galaxytime.service.Calculations
import jp.lab75.galaxytime.service.getReferenceDataFromThemeName

import kotlin.math.roundToInt

private lateinit var backgroundImage: Bitmap

private val padding = 25f
private val typePadding = 20f
private val size = 20f
private val fontSize = 20f
private val fontSizeSM = 16f

//
//
//

fun renderAstronomicsView(
	context: Context,
	canvas: Canvas,
	bounds: Rect,
	textStyle: Paint,
	calculations: Calculations,
	watchFaceData: WatchFaceData,
) {

	var watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	val name = watchFaceData.activeColorStyle.toString()
	val body = calculations.getBodyFromThemeName( name )
	val planetData = if ( body != null ) { calculations.getBodyData( body ) } else { null }

	Log.d("Astronomics", "${name} ${body} ${planetData} ${calculations.bodyList.size}")

	if ( planetData == null ) {
		// no data error
		textStyle.color = watchFaceColors.activeOuterElementColor
		textStyle.textAlign = Paint.Align.CENTER
		textStyle.textSize = fontSize
		addText(bounds.exactCenterX(), bounds.exactCenterY() / 2 + 25f, "NO DATA", textStyle, canvas)
		return
	} else {

		val resources: Resources = context.resources
		val refData = getReferenceDataFromThemeName(name)
		val sunRefData = getReferenceDataFromThemeName("SUN")

		val l1 = "AZI ${
			planetData.horizontal.azimuth.roundToInt().or(272)
		}° ELE ${planetData.horizontal.altitude.roundToInt().or(-2)}°"

		val ras = "RAS %02dH:%02dM:%02dS"
		val l2 = ras.format(
			planetData.rightAscension.degrees.or(12),
			planetData.rightAscension.minutes.or(14),
			planetData.rightAscension.seconds.roundToInt().or(16)
		)

		val dec = "DEC %02d˚%02d'%02d\""
		val l3 = dec.format(
			planetData.declination.degrees.or(12),
			planetData.declination.minutes.or(14),
			planetData.declination.seconds.roundToInt().or(16)
		)

		// ?
		val r1 = "24H 37M"
		// ROT DURATION
		val dayLength = if ( refData?.totalRotationTimeHours!= null ) { (refData.totalRotationTimeHours / 24).toInt() } else { "UNKNOWN"}
		val r2 = "${dayLength} EARTH DAYS"
		// SURFACE
		val r3 = "1.63118 × 1011 km³"
		val r4 = "" // satellites

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
		val centerX = bounds.exactCenterX()
		val centerY = bounds.exactCenterY()

		// distance to sun graph
		val y = centerY - 50f
		val locationRadius = if ( refData?.radius!= null ) { refData.radius.toFloat() } else { 20f }
		val sunRadius = if ( sunRefData?.radius!= null ) { sunRefData.radius.toFloat() } else { 20f }

		canvas.drawLine( centerX - 130f + locationRadius, y, centerX + 130 - sunRadius, y, lineStyle )
		canvas.drawCircle( centerX - 130, y, locationRadius, lineStyle ) // location
		canvas.drawCircle( centerX + 130, y, sunRadius, lineStyle ) // sun

		// header
		textStyle.color = watchFaceColors.activeOuterElementColor
		textStyle.textSize = fontSize
		textStyle.textAlign = Paint.Align.CENTER
		val destination = "SUN"
		addText(centerX, centerY / 2 + 25f, "$name › $destination", textStyle, canvas)

		// planet details
		val xoff_left = 160f
		val xoff_right = 20f
		val yOff = centerY - 20f
		textStyle.textSize = fontSizeSM
		textStyle.textAlign = Paint.Align.LEFT

		addText(centerX - xoff_left, yOff + 30f, l1, textStyle, canvas)
		addText(centerX - xoff_left, yOff + 50f, l2, textStyle, canvas)
		addText(centerX - xoff_left, yOff + 70f, l3, textStyle, canvas)

		addText(centerX + xoff_right, yOff + 30f, r1, textStyle, canvas)
		addText(centerX + xoff_right, yOff + 50f, r2, textStyle, canvas)
		addText(centerX + xoff_right, yOff + 70f, r3, textStyle, canvas)
		addText(centerX + xoff_right, yOff + 90f, r4, textStyle, canvas)

	}
}


