package jp.lab75.galaxytime.views

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import jp.lab75.galaxytime.components.MinimalCompass
import jp.lab75.galaxytime.service.Calculations
import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt


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

		context = applicationContext
		calculations = Calculations.getInstance( context )

		val bun = intent.extras
		val name = (bun?.getString("name") ?: "unknown")
		val c = bun?.getInt("color") ?: "0xffff00ff"
		val color = Color( c as Int )
		val body = calculations.getBodyFromThemeName(name)
		val data = calculations.getBodyData(body)

		Log.d(TAG, "${data?.horizontal?.azimuth} / ${data?.horizontal?.altitude}")
		Log.d(TAG, "${data?.equatorial?.ra} / ${data?.equatorial?.dec}")

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
		// val isMagneticFieldSensorPresent = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

		setTheme(android.R.style.Theme_DeviceDefault)

		setContent {
			GalaxyTimeTheme {
				CompassApp(
					degrees = degrees.value,
					color = color,
					name = name,
					calculations = calculations,
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
fun AttributedData(
	txt: String = "Data",
	att: String = "Attribute",
	color: Color = Color(0xffff00ff)
) {
	val txtSize = 8
	val attSize = 6

	Box(
		modifier = Modifier.wrapContentWidth(),
	) {
		Text(
			color = color,
			fontFamily = FontFamily.Monospace,
			text = txt,
			fontSize = txtSize.sp
		)
		Text(
			modifier = Modifier.offset( y = txtSize.dp),
			color = color,
			fontFamily = FontFamily.Monospace,
			text = att,
			fontSize = attSize.sp,
		)
	}
}

fun degreeToDirection(degrees: Int): String {
	return when (degrees) {
		in (0..22) -> "N"
		in (23..67) -> "NW"
		in (68..112) -> "W"
		in (113..157) -> "SW"
		in (158..202) -> "S"
		in (203..247) -> "SE"
		in (248..292) -> "E"
		in (293..337) -> "NE"
		in (338..360) -> "N"
		else -> ""
	}
}

@Composable
fun CompassApp(
	degrees: Int,
	calculations: Calculations,
	color: Color,
	name: String,
	callback: () -> Unit,
) {

	fun Double.roundTo(decimals: Int = 2): Double = round(this * 10.0.pow(decimals)) / 10.0.pow(decimals)

	val data = calculations.getDataFromName(name)
	val azi = data?.horizontal?.azimuth?.roundTo(2)
	val alt = data?.horizontal?.altitude?.roundTo(2)
	val ra =  data?.equatorial?.ra?.roundTo(2)
	val dec = data?.equatorial?.dec?.roundTo(2)
	val dst = data?.equatorial?.dist?.roundTo(2)

	Log.d("compass","$name azi $azi alt $alt ra $ra dec $dec")

	val off = 16.dp

	MinimalCompass(
		degrees = degrees,
		azi = azi,
		callback = callback
	)

	Box(
		modifier = Modifier
			.drawBehind {
				drawRect(
					color = color,
					size = size,
					blendMode = BlendMode.Multiply
				)
			}
			.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Text(
			modifier = Modifier
				.offset(y=-10.dp)
				.fillMaxWidth(),
			textAlign = TextAlign.Center,
			fontFamily = FontFamily.Monospace,
			color = Color(0xffffffff),
			fontSize = 10.sp,
			text = "${degrees}˚ ${degreeToDirection(degrees)}",
		)
		Text(
			modifier = Modifier
				.offset(y = off)
				.fillMaxWidth(),
			color = color, //Color(0xffffffff),
			text = name,
			textAlign = TextAlign.Center,
			fontSize = 8.sp,
		)
		Row(
			Modifier.offset( y = off + 16.dp ),
		){
			AttributedData("${azi}˚","AZIMUTH", color)
			Spacer( modifier = Modifier.width(10.dp) )
			AttributedData("${alt}˚","ALTITUDE", color)
			Spacer( modifier = Modifier.width(10.dp) )
			AttributedData("${dst}AU","DISTANCE", color)
		}
	}
//	Box(
//		modifier = Modifier
//			.fillMaxSize()
//			.drawBehind {
//				drawRect(
//					color = color,
//					size = size,
//					blendMode = BlendMode.Multiply
//				)
//			}
//	)
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun CompassPreview() {

	val context = LocalContext.current
	val calculations = Calculations.getInstance( context )
//	val ref = getReferenceDataFromThemeName( name )

	val color = Color.Cyan
	val name = "PLANET"

	GalaxyTimeTheme {
		CompassApp(
			degrees = 1337,
			color = color,
			name = name,
			calculations = calculations,
			callback = { }
		)
	}

}


