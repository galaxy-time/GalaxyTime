package jp.lab75.galaxytime.views

import android.util.Log
import android.os.Bundle
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import jp.lab75.galaxytime.R

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

	Image(
		painter = painterResource(R.drawable.sgt_hydration),
		"image",
		Modifier.fillMaxSize(),
		contentScale = ContentScale.Fit
	)
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Text(
			modifier = Modifier.align(Alignment.Center),
			textAlign = TextAlign.Left,
			fontFamily = FontFamily.Monospace,
			color = color,
			text = "BIOMETRICS",
			fontSize = 8.sp,
			lineHeight = 10.sp,
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
