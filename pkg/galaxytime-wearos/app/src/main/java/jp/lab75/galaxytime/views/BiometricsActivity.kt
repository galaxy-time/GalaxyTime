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
import androidx.compose.foundation.layout.Box

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

import kotlin.math.roundToInt

import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import jp.lab75.galaxytime.components.MinimalCompass

class BiometricsActivity : ComponentActivity() {

	private lateinit var context: Context

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setTheme(android.R.style.Theme_DeviceDefault)
		Log.d(TAG, "onCreate()")
		setContent {
			GalaxyTimeTheme {
				BiometricsView(
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

@Preview
@Composable
fun BiometricsView(
	callback: () -> Unit
) {
	Box(modifier = Modifier.fillMaxWidth()) {
		Text(text="Biometrics")
	}
}



	// private fun showSettingsDialog() {
	// 	Log.d(TAG, "showSettingsDialog")
	// 	AlertDialog.Builder(this)
	// 		.setMessage("Some permissions are missing. The app needs location and calendar read permissions to function properly. Please allow them in app settings.")
	// 		.setPositiveButton("App Settings") { _, _ ->
	// 			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
	// 				data = Uri.fromParts("package", packageName, null)
	// 			}
	// 			startActivity(intent)
	// 			finish()
	// 		}
	// 		.setNegativeButton("Cancel") { dialog, _ ->
	// 			dialog.dismiss()
	// 			finish()
	// 		}
	// 		.create()
	// 		.show()
	// }

//	private fun showCompass() {
//		Log.d(TAG, "showCompass()")
//
//		// on tap left button finish this activity
//		// finish()
//	}

