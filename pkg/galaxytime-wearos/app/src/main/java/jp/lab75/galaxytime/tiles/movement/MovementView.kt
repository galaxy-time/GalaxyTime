package jp.lab75.galaxytime

import android.content.Context
import android.content.res.Resources

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF

import kotlin.math.cos
import kotlin.math.sin

import jp.lab75.galaxytime.data.watchface.ColorStyleIdAndResourceIds
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import jp.lab75.galaxytime.data.watchface.WatchMode

fun lerp(start: PointF, end: PointF, t: Float): PointF {
    val x = start.x + (end.x - start.x) * t
    val y = start.y + (end.y - start.y) * t
    return PointF(x, y)
}

fun drawArrow(canvas: Canvas, center: PointF, direction: Float) {
    val paint = Paint()
    paint.color = Color.WHITE
    paint.style = Paint.Style.FILL

    val rotationMatrix = Matrix()
    rotationMatrix.postRotate(direction, center.x, center.y)

    val arrowHead = Path()
    arrowHead.moveTo(center.x, center.y - 10)
    arrowHead.lineTo(center.x - 10, center.y - 20)
    arrowHead.lineTo(center.x, center.y - 30)
    arrowHead.lineTo(center.x + 10, center.y - 20)
    arrowHead.close()

    canvas.save()
    canvas.concat(rotationMatrix)
    canvas.drawPath(arrowHead, paint)
    canvas.restore()
}

fun renderMovementView(
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

    val target_direction = 234f
    val target = PointF(centerX, centerY)
    val current = lerp(target, target, 0.5f)

    drawArrow(canvas, current, target_direction)

}