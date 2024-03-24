package jp.lab75.galaxytime.service

import java.util.Calendar
import java.util.TimeZone
import java.time.ZonedDateTime
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

import androidx.wear.watchface.style.UserStyleSetting

import io.github.cosinekitty.astronomy.*
import jp.lab75.galaxytime.WatchFaceCanvasRenderer
import jp.lab75.galaxytime.utils.COLOR_STYLE_SETTING
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.reflect.typeOf
import kotlin.system.measureTimeMillis

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

	private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

	private var latLonElevNull = Triple(0.0, 0.0, 0.0)
	private var latLonElev = latLonElevNull

	private val bodyDataMap = mutableMapOf<Body, Data>()
	private val bodyList = setOf(
//		Body.Sun,
		Body.Mercury, Body.Venus,
		Body.Earth, Body.Moon, Body.Mars,
		Body.Jupiter, Body.Saturn,
		Body.Uranus, Body.Neptune,
		Body.Pluto
	)

	private var name = "Earth"
	fun setName( nextName: String ) {
		name = nextName
		setCurrentBody(nextName)
	}
	fun getName(): String {
		return name
	}

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
				.setPriority(Priority.PRIORITY_LOW_POWER)
				.build(), null
		).addOnSuccessListener { location: Location? ->
//			Log.d(TAG,"Update location $location");
			if (location != null) {
				this.latLonElev = Triple(location.latitude, location.longitude, location.altitude)
			}
		}
	}

	private fun convertToDMS(x: Double): DMS {
		val negative = x < 0
		val absX = abs(x)

		var degrees = absX.toInt()
		var minutes = ((absX - degrees) * 60).toInt()
		var seconds = (10 * ((absX - degrees - minutes / 60.0) * 3600)).roundToInt() / 10.0

		if (seconds == 60.0) {
			seconds = 0.0
			if (++minutes == 60) {
				++degrees
			}
		}

		return DMS(degrees, minutes, seconds, negative)
	}

//	private fun dmsToTime( d: Int, m: Int, s: Int, tRotation: Int ): PT {
//
//		val totalDegrees = d + m / 60f + s / 3600f
//		val rotationFraction = totalDegrees / 360f
//		val timeInSeconds = ( rotationFraction * tRotation )
//		val days = 0f
//		val hours = (timeInSeconds / 3600f)
//		val minutes = ((timeInSeconds % 3600f) / 60f)
//		val seconds = (timeInSeconds % 60f)
//
//		// Log.d(TAG, "dmsToTime():   $d, $m, $s, $totalDegrees, $rotationFraction, $timeInSeconds")
//		// Log.d(TAG, "dmsToTime():   $days, $hours, $minutes, $seconds")
//
//		return PT( days.toInt(), hours.toInt(), minutes.toInt(), seconds.toInt(), timeInSeconds.toInt() )
//	}

//	private fun calculateDayValue(): Double {
//		val now = Calendar.getInstance().timeInMillis
//
//		// Create a calendar instance for January 1, 2000
//		val year2000 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
//			set(Calendar.YEAR, 2000)
//			set(Calendar.MONTH, 0) // Months are 0-based in Calendar
//			set(Calendar.DAY_OF_MONTH, 1)
//			set(Calendar.HOUR_OF_DAY, 0)
//			set(Calendar.MINUTE, 0)
//			set(Calendar.SECOND, 0)
//			set(Calendar.MILLISECOND, 0)
//		}
//
//		// Calculate the difference in days
//		return 1.0 + (now - year2000.timeInMillis) / (3600.0 * 24.0 * 1000.0)
//	}

//	fun getCompassDirection( observer: Observer, time: ZonedDateTime, body: Body): Int {
////		Log.d(TAG, "getCompassDirection()")
//		return 0
//	}


	fun getPlanetPosition(name:String) :Triple<Double,Double,Double> {
		val planet = getBodyFromThemeName(name)
		val time = Time.fromMillisecondsSince1970( Calendar.getInstance().timeInMillis )
		val observer = Observer( latLonElevNull.first , latLonElevNull.second, latLonElevNull.third )
		val equatorial = equator(planet, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)
		return Triple( equatorial.dist, equatorial.ra, equatorial.dec )
	}

	fun update() {

		val it = getBodyFromThemeName( name )
		val time = Time.fromMillisecondsSince1970( Calendar.getInstance().timeInMillis )
		val observer = Observer( latLonElevNull.first , latLonElevNull.second, latLonElevNull.third )

		// right ascension == longitude
		// declination == latitude
		val equatorial = equator(it, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)
//		Log.d(TAG, "equatorial ${it.name}: ${equatorial.dist.round(2)}AU  ${equatorial.ra.round(2)}˚ ${equatorial.dec.round(2)}˚")

		val horizontal = horizon(time, observer, equatorial.ra, equatorial.dec, Refraction.Normal)

		//

		val convertedRa: DMS = if (it != Body.Earth) {
			convertToDMS(equatorial.ra)
		} else {
			val localTime = Calendar.getInstance()
			DMS(
				localTime.get(Calendar.HOUR_OF_DAY),
				localTime.get(Calendar.MINUTE),
				localTime.get(Calendar.SECOND).toDouble(),
				false
			)
		}
		val convertedDec = convertToDMS(equatorial.dec)

		//

		// val totalRotationTimeHours = getReferenceDataFromThemeName( it.name ).totalRotationTimeHours
		// val elapsedAngle = equatorial.ra
		// val elapsedFraction = elapsedAngle / 360

		val rotation = rotationAxis( it, time )
		val distance =  helioDistance( it, time ).roundTo(2)
		// val apsis = searchPlanetApsis( it, timeA )
		val totalSolarDays = planetOrbitalPeriod(it).roundTo(0)

		// val spin ="%.4f".format(rotation.spin).padStart(13)

		//

		// Log.d("Calc","${it.name.padEnd(12)} ${spin} ${totalSolarDays.toString().padStart(12)}")
		// Log.d("Calculations","---- update ------------------------")
		// Log.d("Calculations","Body\t${it.name}")
		// Log.d("Calculations","RA           ${equatorial.ra}")
		// Log.d("Calculations","Body\t${it.name}\t\t\tRA ${equatorial.ra/24*360} \t\t${convertedRa}")
		// Log.d("Calculations","DEC          ${equatorial.dec}")
		// Log.d("Calculations","horizontal   ${horizontal.ra}")
		// Log.d("Calculations","Azimuth      ${horizontal.azimuth}")
		// Log.d("Calculations","Altitude     ${horizontal.altitude}")
		// Log.d("Calculations","ConvertedRA  ${convertedRa.degrees}h ${convertedRa.minutes}m ${convertedRa.seconds}s ${convertedRa.negative}")
		// Log.d("Calculations","ConvertedDec ${if (convertedDec.negative) "-" else ""}${convertedDec.degrees}° ${convertedDec.minutes}' ${convertedDec.seconds}\" ${convertedDec.negative}")
		// Log.d("Calc","${it.name}\t\t ${rotationAngle.ra} ${horizontal.azimuth} / ${horizontal.altitude} ${distance}")
		// Log.d("Calc","${apsis.time}, ${apsis.kind}")
		// Log.d("Calc","${it.name}\t\t\t\t total ${totalRotationTimeHours}\t elapsed ${elapsedHours}")

		//

		bodyDataMap[it] = Data(
			equatorial = equatorial,
			horizontal = horizontal,
			rightAscension = convertedRa,
			declination = convertedDec,
			// time,
			// totalRotationTimeHours.toInt(),
			rotation = rotation,
			totalSolarDays = totalSolarDays,
			distance = distance
		)
	}

	private fun Double.roundTo(decimals: Int = 2): Double = round(
		this * 10.0.pow(
			decimals
		)
	) / 10.0.pow(decimals)

//	fun calculateRASeconds(raDegrees: Float, totalRotationTimeSeconds: Int): Float {
//		return (raDegrees / 360f) * totalRotationTimeSeconds
//	}

//	fun convertToPlanetTime( ra: Double, totalRotationTimeHours: Float): Triple<Int, Int, Int> {
//
//		val elapsedSeconds = calculateRASeconds( ra.toFloat(), (totalRotationTimeHours * 3600).toInt() )
//    	val elapsedRotationTimeHours = elapsedSeconds / 3600
//		// Log.d("Calc","RA ${ra} \t\t—— ${totalRotationTimeHours} \t\t—— ${elapsedRotationTimeHours}")
//
//		val hours = (elapsedRotationTimeHours % totalRotationTimeHours).toInt()
//		val minutes = ((elapsedSeconds % 3600) / 60)
//		val seconds = (elapsedSeconds % 60)
//
//		// Log.d("Calc", "\t\t\t\tTime ${hours}:${minutes}:${seconds} —— ${totalRotationTimeHours}")
//    	return Triple(hours.toInt(), minutes.toInt(), seconds.toInt())
//	}

}
