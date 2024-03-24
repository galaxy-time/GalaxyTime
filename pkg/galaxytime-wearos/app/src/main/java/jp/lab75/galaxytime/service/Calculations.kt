package jp.lab75.galaxytime.service

import java.util.Calendar
import kotlin.math.roundToInt

import android.util.Log
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import io.github.cosinekitty.astronomy.*
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Calculations private constructor(private val context: Context) {

	// Singleton
	companion object {
		@Volatile private var INSTANCE: Calculations? = null // Volatile modifier is necessary
		fun getInstance(context: Context) =
			INSTANCE ?: synchronized(this) { // synchronized to avoid concurrency problem
				INSTANCE ?: Calculations(context).also { INSTANCE = it }
			}
		private const val TAG = "Calculations"
	}
	init {
	    Log.d(TAG,"init()")
	}

	//

	private val _name = MutableStateFlow<String>( "Earth" )
	val name: StateFlow<String> = _name

	private var prevName = "Earth"
	fun setName( nextName: String ) {
		if ( prevName == nextName || nextName == "" ) return
		_name.value = nextName
		setCurrentBody( nextName )
		update()
		Log.d(TAG,"setName($nextName) -> ${name.value} -> $_name.value ")
	}
	//

	private val _dayOfYear = MutableStateFlow<Double>( 0.toDouble() )
	val dayOfYear: StateFlow<Double> = _dayOfYear

	data class LocalTime(var dd:Int, var hh:Int, var mm:Int, var ss:Int )
	private val _localTime = MutableStateFlow<LocalTime>( LocalTime( dd=0, hh=0, mm=0, ss=0 ) )
	val localTime: StateFlow<LocalTime> = _localTime

	//

	private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
	private var latLonElevNull = Triple(0.0, 0.0, 0.0)
	private var latLonElev = latLonElevNull

	//

	private val bodyDataMap = mutableMapOf<Body, Data>()
	private val bodyList = arrayOf(
		Body.Mercury, Body.Venus,
		Body.Earth, Body.Moon, Body.Mars,
		Body.Jupiter, Body.Saturn,
		Body.Uranus, Body.Neptune,
		Body.Pluto
	)

	private var currentBody: Body = Body.Earth
	private fun setCurrentBody(name: String) {
		currentBody = bodyList.find { it.name.lowercase() == name.lowercase() } ?: Body.Earth
	}
	fun getCurrentBody(): Body { return currentBody }
	fun getCurrentBodyData(): Data? { return bodyDataMap[currentBody] }

	fun getBodyFromThemeName(name: String): Body {
		return bodyList.find { it.name.lowercase() == name.lowercase() } ?: Body.Earth
	}
	fun getBodyData(body: Body): Data? {
		return bodyDataMap[body]
	}
	fun getDataFromName( name: String ): Data? {
		return getBodyData( getBodyFromThemeName(name) )
	}

	fun updateLocation() {
		if (ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.ACCESS_COARSE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {
			// TODO: Extract permission check and...
			Log.d(TAG, "No permissions")
			return
		}

		fusedLocationClient.getCurrentLocation(
			CurrentLocationRequest.Builder().setDurationMillis(10000)
				.setMaxUpdateAgeMillis(10000)
				.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
				.build(), null
		).addOnSuccessListener { location: Location? ->
			Log.d(TAG,"Update location $location");
			if (location != null) {
				this.latLonElev = Triple(location.latitude, location.longitude, location.altitude)
			}
		}
	}

	private fun convertToDMS(x: Double): DMS {

		var tempX = x
		val negative = tempX < 0
		if (negative) { tempX = -tempX }
		var degrees = tempX.toInt()
		tempX = 60.0 * (tempX - degrees)
		val minutes = tempX.toInt()
		tempX = 60.0 * (tempX - minutes)
		// Round to the nearest tenth of an arcsecond.
		var seconds = (10.0 * tempX).roundToInt() / 10.0
		if (seconds == 60.0) {
			seconds = 0.0
			tempX = minutes + 1.0
			if (tempX == 60.0) {
				tempX = 0.0
				++degrees
			}
		}
		return DMS(degrees, minutes, seconds, negative)

	}

	fun update() {

		if ( _name.value == ""  ) return
		val it = getBodyFromThemeName( _name.value )

		val time = Time.fromMillisecondsSince1970( Calendar.getInstance().timeInMillis )

		val observer = Observer( latLonElev.first , latLonElev.second, latLonElev.third )
		val equatorial = equator(it, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)
		val horizontal = horizon(time, observer, equatorial.ra, equatorial.dec, Refraction.Normal)

		val localTime = Calendar.getInstance()

		val convertedRa: DMS =
			if (it != Body.Earth) {
				convertToDMS(equatorial.ra)
			} else {
				DMS(
					localTime.get(Calendar.HOUR_OF_DAY),
					localTime.get(Calendar.MINUTE),
					localTime.get(Calendar.SECOND).toDouble(),
					false
				)
			}
		val convertedDec = convertToDMS(equatorial.dec)

		// TODO: calculate correct offset per year
		// ref: https://github.com/cosinekitty/astronomy/discussions/336

		val rotation = rotationAxis( it, time )
		val distance =  helioDistance( it, time ).round(2)
		val totalSolarDays = planetOrbitalPeriod(it).round(0)

		//

		val totalAngle = 360f
		val currentAngle = eclipticLongitude( it, time )
		val currentDay = ( currentAngle * ( totalSolarDays / totalAngle ) )
		_dayOfYear.value = currentAngle
		// Log.d(TAG,"$el ———— $doy")

		val earthTime = LocalTime(
			localTime.get(Calendar.DAY_OF_YEAR),
			localTime.get(Calendar.HOUR_OF_DAY),
			localTime.get(Calendar.MINUTE),
			localTime.get(Calendar.SECOND)
		)
		val hours = equatorial.ra / 15f
		val minutes = ( hours - hours.toInt() ) * 60f
		val seconds = ( minutes - minutes.toInt() ) * 60f
		val spaceTime = LocalTime(
			currentDay.toInt(),
			hours.toInt(),
			minutes.toInt(),
			seconds.toInt()
		)

		_localTime.value = if( _name.value == "Earth" ) earthTime else spaceTime
		Log.d(TAG,"${time.toMillisecondsSince1970()} —— $it —— ${horizontal.ra} —— ${equatorial.ra} —— ${_localTime.value} —— $hours $minutes $seconds")

		bodyDataMap[it] = Data(
			equatorial = equatorial,
			horizontal = horizontal,
			rightAscension = convertedRa,
			declination = convertedDec,
			rotation = rotation,
			distance = distance,
			totalSolarDays = totalSolarDays,
			dayOfYear = currentDay
		)

	}

	private fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()

}
