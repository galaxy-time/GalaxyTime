package jp.lab75.galaxytime.views

import android.util.Log
import android.os.Bundle
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.addText

import kotlin.math.roundToInt

import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import jp.lab75.galaxytime.components.MinimalCompass
import jp.lab75.galaxytime.service.Calculations
import jp.lab75.galaxytime.service.getReferenceDataFromThemeName

class BiometricsActivity : ComponentActivity() {

	lateinit var context: Context
	private lateinit var calculations: Calculations

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setTheme(android.R.style.Theme_DeviceDefault)

		context = applicationContext
		calculations = Calculations.getInstance(context)

		val bun = intent.extras
		val name = (bun?.getString("name") ?: "unknown")
		val c = bun?.getInt("color") ?: "0xffff00ff"
		val color = Color( c as Int )

		setContent {
			GalaxyTimeTheme {
				BiometricsView(
					name = name,
					color = color,
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
	callback: () -> Unit = {}
) {
	val haptic = LocalHapticFeedback.current

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
		Text(
			modifier = Modifier.align(Alignment.Center).offset( x = -40.dp, y = 20.dp ),
			textAlign = TextAlign.Right,
			fontFamily = FontFamily.Monospace,
			color = Color.White,
			text = "HYDRATION HRS",
			fontSize = 6.sp,
			lineHeight = 6.sp,
		)

		Text(
			modifier = Modifier.align(Alignment.Center).offset( x = -45.dp, y = 40.dp )
			.border(width = 0.5.dp, color = Color.White)
			.pointerInput(Unit) {
					detectTapGestures(
						onTap = {
							haptic.performHapticFeedback(HapticFeedbackType.LongPress)
							println("on tap")
						}
					)
				}.drawBehind {
							 drawRect(
								 color = Color(0xffff00ff),
								 alpha = 0f,
								 size = size
							 )
				}.padding(3.dp),
			textAlign = TextAlign.Right,
			fontFamily = FontFamily.Monospace,
			color = Color.White,
			text = "TAP TO CONFIRM\nREHYDRATION",
			fontSize = 6.sp,
			lineHeight = 6.sp,
		)
		Text(
			modifier = Modifier.align(Alignment.Center).offset( x = 35.dp, y = 22.dp ),
			textAlign = TextAlign.Left,
			fontFamily = FontFamily.Monospace,
			color = Color.White,
			text = "LOW",
			fontSize = 12.sp,
			lineHeight = 6.sp,
		)
		DrawScale()
	}

}

@Composable
fun DrawScale() {

	val items = arrayOf("0", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8")
	val text = items.joinToString(separator = "\n")

	Box(
		modifier = Modifier.drawBehind {
			val cx = size.width / 2f + 15f
			val cy = size.height / 2f
			drawLine(
				start = Offset(cx, 0f),
				end = Offset(cx, size.height),
				color = Color(0xffffffff),
				strokeWidth = 1f
			)
		},
		contentAlignment = Alignment.Center,
	) {
		Text(
			modifier = Modifier
				.wrapContentWidth()
				.padding(10.dp),
//				.offset(x = -8.dp),
			textAlign = TextAlign.Right,
			fontFamily = FontFamily.Monospace,
			color = Color.White,
			text = text,
			fontSize = 6.sp,
			lineHeight = 20.sp,
		)
	}
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun BiometricsPreview() {
	GalaxyTimeTheme {
		BiometricsView()
	}
}
