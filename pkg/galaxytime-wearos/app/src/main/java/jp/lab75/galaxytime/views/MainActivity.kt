/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package jp.lab75.galaxytime.views

import android.hardware.SensorEventListener as SensorEventListener1
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

import jp.lab75.galaxytime.*
import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.theme.GalaxyTimeTheme

class MainActivity : ComponentActivity(), SensorEventListener1 {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null

	override fun onCreate(savedInstanceState: Bundle?) {

		installSplashScreen()

		super.onCreate(savedInstanceState)
		setTheme(android.R.style.Theme_DeviceDefault)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

		setContent {
			WearApp("GalaxyTime")
		}

	}

	override fun onResume() {
        super.onResume()
        // Register the listener
        heartRateSensor?.also { heartRate ->
            sensorManager.registerListener(this, heartRate, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't receive any more updates
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0]
            // Update your UI here with the new heart rate value
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }
	companion object {
        const val TAG = "MainActivity"
	}

}

@Composable
fun WearApp(greetingName: String) {
	GalaxyTimeTheme {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colors.background),
			contentAlignment = Alignment.Center
		) {
			// TimeText()
			Greeting(greetingName = greetingName)
		}
	}
}

@Composable
fun Greeting(greetingName: String) {
	Text(
		modifier = Modifier.fillMaxWidth(),
		textAlign = TextAlign.Center,
		color = MaterialTheme.colors.primary,
		text = stringResource(R.string.hello_world, greetingName)
	)
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
	WearApp("GalaxyTime Preview")
}
