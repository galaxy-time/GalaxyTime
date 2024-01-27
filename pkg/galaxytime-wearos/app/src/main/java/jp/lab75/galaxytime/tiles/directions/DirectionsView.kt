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
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.BlendMode

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
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
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

private lateinit var backgroundImage: Bitmap
private val padding = 25f
private val typePadding = 20f
private val size = 20f
private val fontSize = 20f

//
//
//

fun drawCompass( context: Context, canvas: Canvas, bounds: Rect, textStyle: Paint ) {

	val HOUR_MARKS = arrayOf("E", "S", "W", "N")
	var radius = 100f
	val textBounds = Rect()


	for (i in 0 until 4) {

		val rotation = 0.5f * (i + 1).toFloat() * Math.PI
		val dx = sin(rotation).toFloat() * radius * bounds.width().toFloat()
		val dy = -cos(rotation).toFloat() * radius * bounds.width().toFloat()

		textStyle.getTextBounds(HOUR_MARKS[i], 0, HOUR_MARKS[i].length, textBounds)

		canvas.drawText(
			HOUR_MARKS[i],
			bounds.exactCenterX() + dx - textBounds.width() / 2.0f,
			bounds.exactCenterY() + dy + textBounds.height() / 2.0f,
			textStyle
		)

	}

}

	//
//
//

fun renderDirectionsView(
	context: Context,
	canvas: Canvas,
	bounds: Rect,
	textStyle: Paint,
) {

	val resources: Resources = context.resources

	var watchFaceData: WatchFaceData = WatchFaceData()
	var watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	val t_n = "N"
	val t_e = "E"
	val t_s = "S"
	val t_w = "W"

	val target_name = watchFaceData.activeColorStyle.toString()
	val target_info = "1234˚ NW"
	val target_direction = 234f

	// background

	// val backgroundBitmap = BitmapFactory.decodeResource( resources, R.drawable.sgt_planet_neutral )
	// backgroundImage = Bitmap.createScaledBitmap( backgroundBitmap, bounds.width(), bounds.height(), false )
	// canvas.drawBitmap( backgroundImage, bounds, bounds, null )

	// val overlayColor = watchFaceColors.activePrimaryColor
	// canvas.drawColor( overlayColor, BlendMode.OVERLAY )

	// view

	drawCompass( context, canvas, bounds, textStyle )
	// drawArrow( canvas, 123f )
	// drawInfo( canvas )

	fun drawArrow( canvas: Canvas, direction:Float ) {

		val lineStyle = Paint().apply {
			isAntiAlias = true
			strokeWidth = 1f
			style = Paint.Style.STROKE
			color = Color.WHITE
		}

	}

	val lineStyle = Paint().apply {
        isAntiAlias = true
		strokeWidth = 1f
		style = Paint.Style.STROKE
		color = Color.WHITE
    }

	val centerX = 0.5f * bounds.width().toFloat()
	val centerY = 0.5f * bounds.height().toFloat()

	canvas.drawLine(
		centerX,
		0f,
		bounds.width().toFloat() - 105f ,
		centerY - 20f,
		lineStyle
	)

	textStyle.color = watchFaceColors.activeOuterElementColor
	textStyle.textSize = fontSize
	textStyle.textAlign = Paint.Align.CENTER

	val destination = "EARTH"
	val location  = watchFaceData.activeColorStyle.toString()
	addText( centerX, centerY - 140f, "$location — $destination", textStyle, canvas )

	textStyle.textAlign = Paint.Align.LEFT
	// addText( centerX - 140f, centerY + 30f, l1, textStyle, canvas )
	// addText( centerX - 140f, centerY + 50f, l2, textStyle, canvas )
	// addText( centerX - 140f, centerY + 70f, l3, textStyle, canvas )

	// addText( centerX + 20f, centerY + 30f, r1, textStyle, canvas )
	// addText( centerX + 20f, centerY + 50f, r2, textStyle, canvas )
	// addText( centerX + 20f, centerY + 70f, r3, textStyle, canvas )
	// addText( centerX + 20f, centerY + 90f, r4, textStyle, canvas )


}


