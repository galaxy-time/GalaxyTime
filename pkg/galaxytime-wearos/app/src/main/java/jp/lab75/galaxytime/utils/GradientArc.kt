//
//	a donut filled with a sweep gradient
//

package jp.lab75.galaxytime.utils

import android.R

import android.content.Context
import android.content.res.Resources

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SweepGradient


var next = 0f

fun drawGradientArc ( canvas: Canvas, bounds: Rect, innerRadius: Float, outerRadius: Float, start: Float, segments: Float, color: Int, alpha: Int ) {

	// gradient

	val colors = intArrayOf( Color.BLACK, color.toInt() )

	// next = Math.max(start,next) - Math.min(start,next) / 2f

	// instead of rotating the path, we shift the gradient itself for smoother rendering.
	// val gradientOffset = 1f / segments * start
	// val positions = floatArrayOf( gradientOffset, 1f + gradientOffset )

	val xc = bounds.centerX().toFloat()
	val yc = bounds.centerY().toFloat()

	val positions = floatArrayOf( 0f, 1f )
	val gradient = SweepGradient ( xc, yc, colors, positions )
	val gradientFillPaint = Paint()
		gradientFillPaint.apply {
			shader = gradient
			isAntiAlias = true
		}
    gradientFillPaint.alpha = alpha
	// apply matrix to the gradient instead of the whole canvas:
	val matrix = Matrix()
		matrix.setRotate( start, xc, yc )
	gradient.setLocalMatrix(matrix)


	val clippingPaint = Paint()
		clippingPaint.color = Color.BLACK
		clippingPaint.isAntiAlias = true

	val outerRectF = RectF(
		bounds.exactCenterX() - outerRadius,
		bounds.exactCenterY() - outerRadius,
		bounds.exactCenterX() + outerRadius,
		bounds.exactCenterY() + outerRadius
		)

	val path = Path()
		path.addCircle(outerRectF.centerX(), outerRectF.centerY(), outerRadius, Path.Direction.CW )

	val innerRectf = RectF(
		bounds.exactCenterX() - innerRadius,
		bounds.exactCenterY() - innerRadius,
		bounds.exactCenterX() + innerRadius,
		bounds.exactCenterY() + innerRadius
		)

	val clipping = Path()
		clipping.addCircle( innerRectf.centerX(), innerRectf.centerY(), innerRadius, Path.Direction.CW )

	path.op( clipping, Path.Op.DIFFERENCE )

	val rot = Matrix()
		rot.postRotate( start, outerRectF.centerX(), outerRectF.centerY() )

	// canvas.save()
	// canvas.rotate( start, bounds.exactCenterX(), bounds.exactCenterY() )
	// canvas.save()
	canvas.drawPath( path, gradientFillPaint )
	// canvas.restore()
	// canvas.restore()

}
