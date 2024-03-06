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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

import kotlin.math.roundToInt

import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import jp.lab75.galaxytime.components.MinimalCompass
import jp.lab75.galaxytime.service.Calculations

class CompassActivity : ComponentActivity(), SensorEventListener {

	lateinit var context: Context
	lateinit var calculations: Calculations

	private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val mOrientationAngles = FloatArray(3)

 	private val degrees: MutableState<Int> = mutableStateOf(0)

	// private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
	// private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
	// private var magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		context = getApplicationContext()
		calculations = Calculations.getInstance( context )

		val bun = intent.extras
		val name = (bun?.getString("name") ?: "unknown")
		val c = bun?.getInt("color") ?: "0xffff00ff"
		val color = Color( c as Int )
		val body = calculations.getBodyFromThemeName(name)
		val data = calculations.getBodyData(body)

		Log.d(TAG, "${data?.horizontal?.azimuth} / ${data?.horizontal?.altitude}")

/*
	TODO: check hourAngle in astronomy-engine
	Finds the hour angle of a body for a given observer and time.
	The hour angle of a celestial body indicates its position in the sky
	with respect to the Earth's rotation. The hour angle depends on the
	location of the observer on the Earth. The hour angle is 0 when the
	body's center reaches its highest angle above the horizon in a given day.
	The hour angle increases by 1 unit for every sidereal hour that passes
	after that point, up to 24 sidereal hours when it reaches the highest
	point again. So the hour angle indicates the number of hours that have
	passed since the most recent time that the body has culminated,
	or reached its highest point.
*/


		sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
		val isMagneticFieldSensorPresent = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

		setTheme(android.R.style.Theme_DeviceDefault)
		setContent {
			GalaxyTimeTheme {
				CompassApp(
					degrees = degrees.value,
					callback = { finishActivity() }
				)
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		finish()
	}

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    override fun onPause() {
        super.onPause()
		Log.d(TAG, "onPause()")
        sensorManager.unregisterListener(this)
    }

 	override fun onResume() {
        super.onResume()
		Log.d(TAG, "onResume()")
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }
	override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        val azimuthInRadians = this.mOrientationAngles[0]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).roundToInt()
		degrees.value = if(azimuthInDegrees < 0 ) azimuthInDegrees + 360
        else azimuthInDegrees

        computeMatrix()
    }

	fun computeMatrix() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, mOrientationAngles)
    }

	fun finishActivity() {
		Log.d(TAG,"exit compass activity")
		sensorManager.unregisterListener(this)
		finish()
	}

	companion object {
		const val TAG = "CompassActivity"
	}

}

@Composable
fun CompassApp(degrees: Int, callback: () -> Unit) {
	MinimalCompass(
		degrees = degrees,
		callback = callback
	)
}

@Composable
fun CompassHello( content: String ) {
	Text(
		modifier = Modifier.fillMaxWidth(),
		textAlign = TextAlign.Center,
		color = MaterialTheme.colors.primary,
		text = content
	)
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

