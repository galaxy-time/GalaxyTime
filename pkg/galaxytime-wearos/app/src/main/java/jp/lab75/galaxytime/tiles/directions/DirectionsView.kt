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
import android.graphics.Matrix
import android.graphics.PointF
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData

//

private lateinit var backgroundImage: Bitmap
private val padding = 25f
private val typePadding = 20f
private val size = 20f
private val fontSize = 20f
private val DIRECTION_MARKS = arrayOf("E", "S", "W", "N")

//

fun drawCompass( context: Context, canvas: Canvas, bounds: Rect, center: PointF, textStyle: Paint ) {

	// val resources: Resources = context.resources
    // val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    // val accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

	//  if (accelerometer != null) {
    //     val accelerometerValues = FloatArray(3)

    //     sensorManager.getSensorData(accelerometer, System.currentTimeMillis(), accelerometerValues)

    //     val rotation = Math.toDegrees(Math.atan2(accelerometerValues[1].toDouble(), accelerometerValues[0].toDouble())).toFloat()

    //     val radius = 0.5f * bounds.width()
    //     canvas.drawCircle(
    //         center.x,
    //         center.y,
    //         radius,
    //         textStyle
    //     )

    //     val lineStyle = Paint().apply {
    //         isAntiAlias = true
    //         strokeWidth = 1f
    //         style = Paint.Style.STROKE
    //         color = Color.WHITE
    //     }

    //     val rotationMatrix = Matrix()
    //     rotationMatrix.postRotate(rotation, center.x, center.y)

    //     canvas.drawLine(
    //         center.x,
    //         center.y - radius,
    //         center.x + radius * 0.75f,
    //         center.y - radius * 0.75f,
    //         lineStyle
    //     )
    //     canvas.drawLine(
    //         center.x,
    //         center.y - radius,
    //         center.x + radius * 0.75f,
    //         center.y + radius * 0.75f,
    //         lineStyle
    //     )
    // }
	// var radius = 100f
	// val textBounds = Rect()

	// for (i in 0 until 4) {

	// 	val rotation = 0.5f * (i + 1).toFloat() * Math.PI
	// 	val dx = sin(rotation).toFloat() * radius * center.x
	// 	val dy = -cos(rotation).toFloat() * radius * center.y

	// 	textStyle.getTextBounds(DIRECTION_MARKS[i], 0, DIRECTION_MARKS[i].length, textBounds)

	// 	addText(
	// 		bounds.exactCenterX() + dx - textBounds.width() / 2.0f,
	// 		bounds.exactCenterY() + dy + textBounds.height() / 2.0f,
	// 		DIRECTION_MARKS[i],
	// 		textStyle,
	// 		canvas
	// 	)

	// }

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

	val center = PointF( bounds.exactCenterX(), bounds.exactCenterY() )

	var watchFaceData: WatchFaceData = WatchFaceData()
	var watchFaceColors = convertToColorPalette(
		context,
		watchFaceData.activeColorStyle,
		watchFaceData.ambientColorStyle
	)

	textStyle.color = watchFaceColors.activeOuterElementColor
	textStyle.textSize = fontSize
	textStyle.textAlign = Paint.Align.CENTER

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

	drawCompass( context, canvas, bounds, center, textStyle )
	drawArrow( canvas, center, 123f )
	// drawInfo( canvas )

	val lineStyle = Paint().apply {
        isAntiAlias = true
		strokeWidth = 1f
		style = Paint.Style.STROKE
		color = Color.WHITE
    }

	val centerX = 0.5f * bounds.width().toFloat()
	val centerY = 0.5f * bounds.height().toFloat()

	// draw arrow

	canvas.drawLine(
		centerX,
		padding,
		bounds.width().toFloat(),
		bounds.height().toFloat(),
		lineStyle
	)
	canvas.drawLine(
		centerX,
		padding,
		0f,
		bounds.height().toFloat(),
		lineStyle
	)

	// draw data

	val location  = watchFaceData.activeColorStyle.toString()
	addText( centerX, centerY - 20f, target_name, textStyle, canvas )
	addText( centerX, centerY + 20f, target_info, textStyle, canvas )

}


