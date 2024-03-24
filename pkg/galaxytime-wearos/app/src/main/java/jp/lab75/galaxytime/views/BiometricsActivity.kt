package jp.lab75.galaxytime.views

import android.util.Log
import android.os.Bundle
import android.content.Context
import android.graphics.Point

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.PathEffect

import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import jp.lab75.galaxytime.service.BiometricsService
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.VectorProperty
import kotlin.math.round
import kotlin.math.roundToInt

class BiometricsActivity : ComponentActivity() {

	lateinit var context: Context
	private lateinit var biometrics: BiometricsService

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setTheme(android.R.style.Theme_DeviceDefault)

		context = applicationContext
		biometrics = BiometricsService.getInstance(context)

		val bun = intent.extras
		val name = (bun?.getString("name") ?: "unknown")
		val c = bun?.getInt("color") ?: "0xffff00ff"
		val color = Color( c as Int )

		setContent {
			GalaxyTimeTheme {
				BiometricsView(
					name = name,
					color = color,
					biometrics = biometrics,
					onHydrate = { biometrics.add() },
					callback = { finishActivity() }
				)
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		finish()
	}

    override fun onPause() {
        super.onPause()
		Log.d(TAG, "onPause()")
    }

 	override fun onResume() {
        super.onResume()
		Log.d(TAG, "onResume()")
    }

	fun finishActivity() {
		Log.d(TAG,"exit biometrics activity")
		finish()
	}

	companion object {
		const val TAG = "BiometricsActivity"
	}

}

@Composable
fun BiometricsView(
	name: String = "BIOMETRICS",
	color: Color = Color(0xffffffff),
	biometrics: BiometricsService,
	onHydrate: () -> Unit = {},
	callback: () -> Unit = {}
) {

	val haptic = LocalHapticFeedback.current
	val state by biometrics.state.collectAsState()

	Log.d("Biometrics","mutable $state")
	Image(
		painter = painterResource(R.drawable.sgt_hydration),
		"image",
		Modifier.fillMaxSize(),
		contentScale = ContentScale.Fit
	)
	Box( modifier = Modifier
		.fillMaxSize()
		.clickable(true) { callback() }
	)
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		DrawScale()
//		DrawGraph()
		LineChart(biometrics = biometrics)
		Box(
			modifier = Modifier
				.width(80.dp)
				.height(50.dp)
				.offset(x = -45.dp, y = 35.dp)
				.pointerInput(Unit) {
					detectTapGestures(
						onTap = {
							haptic.performHapticFeedback(HapticFeedbackType.LongPress)
							onHydrate()
						}
					)
				}
		)
		Text(
			modifier = Modifier
				.align(Alignment.Center)
				.offset(x = -45.dp, y = 15.dp),
			textAlign = TextAlign.Right,
			fontFamily = FontFamily.Monospace,
			color = Color.White,
			text = "HYDRATION HRS",
			fontSize = 6.sp,
			lineHeight = 6.sp,
		)
		Column(
			modifier = Modifier
		){
			Text(
				modifier = Modifier
					.offset(x = -50.dp, y = 40.dp)
					.border(width = 0.5.dp, color = Color.White)
					.drawBehind {
						drawRect(
							color = Color(0xffff00ff),
							alpha = 0f,
							size = size
						)
					}
					.padding(3.dp),
				textAlign = TextAlign.Right,
				fontFamily = FontFamily.Monospace,
				color = Color.White,
				text = "TAP TO CONFIRM\nREHYDRATION",
				fontSize = 6.sp,
				lineHeight = 6.sp,
			)
			Text(
				modifier = Modifier
					.offset(x = 35.dp, y = 22.dp),
				textAlign = TextAlign.Left,
				fontFamily = FontFamily.Monospace,
				color = Color.White,
				text = state,
				fontSize = 12.sp,
				lineHeight = 6.sp,
			)
		}
	}

}

// draw a vertical scale
// to measure hydrations

@Composable
fun DrawScale() {

	val items = arrayOf("0", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8")
	val text = items.joinToString(separator = "\n")

	Box(
		modifier = Modifier.drawBehind {
			val cx = size.width / 2f
			val cy = size.height / 2f
			drawLine(
				start = Offset(cx, 0f),
				end = Offset(cx, size.height),
				color = Color(0xffffffff),
				strokeWidth = 1.dp.toPx(),
			)
		},
		contentAlignment = Alignment.Center,
	) {
		Text(
			modifier = Modifier
				.wrapContentWidth()
				.padding(10.dp)
				.offset(x = -10.dp),
			textAlign = TextAlign.Right,
			fontFamily = FontFamily.Monospace,
			color = Color.White,
			text = text,
			fontSize = 6.sp,
			lineHeight = 20.sp,
		)
	}
}

// draw a hydration graph scrolling to -x
// featuring 10 points which reflect
// the last 10 hydration states
// on their y axis.

@Composable
fun DrawGraph() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.drawBehind {
				val cx = size.width / 2f
				val cy = size.height / 2f
				drawLine(
					start = Offset(0f, cy),
					end = Offset(size.width, cy),
					color = Color(0xffffffff),
					strokeWidth = 1.dp.toPx(),
					cap = StrokeCap.Round, // important!
					pathEffect = PathEffect.dashPathEffect(
						intervals = floatArrayOf(0f, 4.dp.toPx()),
					),
				)
			}
			.alpha(0f)
		)
}

@Composable
fun LineChart(biometrics: BiometricsService) {

	val dataPoints by biometrics.dataPoints.collectAsState()

	Box( modifier = Modifier
		.padding( 10.dp )
		.drawWithCache {

			val stepX = round( size.width / ( dataPoints.size - 1 ) )
			val stepY = round( size.height / 5 )

			val path = Path()
			path.moveTo(0f, ( size.height - ( ( dataPoints.first() * stepY ) / 2000 ) * size.height ) )

			dataPoints.forEachIndexed { index, p ->

				val x = ( stepX * index )
				//			invert			scale		fraction
				val y = ( size.height - ( ( p * stepY ) / 2000 ) * size.height )

				path.lineTo( x, y )

//				Log.d("Biometrics","$index: $x, $y, $p")

			}

			onDrawBehind {
				drawPath(
					path = path,
					color = Color.White,
					style = Stroke( width = 1f ),
					alpha = 0.75f
				)
			}

		}
		.fillMaxSize()
	)

}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun BiometricsPreview() {
	val context = LocalContext.current
	val biometrics = BiometricsService.getInstance(context)
	GalaxyTimeTheme {
		BiometricsView( biometrics = biometrics )
	}
}
