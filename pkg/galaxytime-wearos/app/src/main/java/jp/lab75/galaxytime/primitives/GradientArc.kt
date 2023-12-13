//
//	a donut filled with a sweep gradient
//

package jp.lab75.galaxytime.primitives

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

public fun drawGradientArc ( canvas: Canvas, bounds: Rect, innerRadius: Float, outerRadius: Float, start: Float, color: Int ) {

	// gradient

	val colors = intArrayOf( Color.BLACK, color.toInt() )
	val positions = floatArrayOf(0.0f, 1.0f)
	val gradient = SweepGradient (
		bounds.width().toFloat() / 2f,
		bounds.height().toFloat() / 2f,
		colors,
		positions
	)
	val gradientFillPaint = Paint()
		gradientFillPaint.apply {
			shader = gradient
			isAntiAlias = true
		}

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

	canvas.save()
	canvas.rotate( start, bounds.exactCenterX(), bounds.exactCenterY() )
	canvas.save()
	canvas.drawPath( path, gradientFillPaint )
	canvas.restore()
	canvas.restore()

}
