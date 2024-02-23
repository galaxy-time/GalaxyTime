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

class Calculations(private val context: Context) {

	private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
	private val bodyDataMap = mutableMapOf<Body, Data>()
	private var latLonElevNull = Triple(0.0, 0.0, 0.0)
	private var latLonElev = latLonElevNull

	val bodyList = arrayOf(
		Body.Sun, Body.Mercury, Body.Venus,
		Body.Earth, Body.Moon, Body.Mars,
		Body.Jupiter, Body.Saturn,
		Body.Uranus, Body.Neptune,
		Body.Pluto
	)

	fun getBodyFromThemeName(name: String): Body? {
		return bodyList.find { it.name.lowercase() == name.lowercase() }
	}

	fun getBodyData(body: Body): Data? {
		if (!bodyDataMap.containsKey(body)) {
			return bodyDataMap[getBodyFromThemeName("Earth")]
		}
		return bodyDataMap[body]
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
			// Open permission dialog
			// TODO: Extract permission check and...
			Log.d("Calculations", "No permissions")
			return
		}

		fusedLocationClient.getCurrentLocation(
			CurrentLocationRequest.Builder().setDurationMillis(10000)
				.setMaxUpdateAgeMillis(10000)
				.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
				.build(), null
		).addOnSuccessListener { location: Location? ->
			Log.d("Calculations","Update location $location");
			if (location != null) {
				this.latLonElev = Triple(location.latitude, location.longitude, location.altitude);
			}
		}
	}

	private fun convertToDMS(x: Double): DMS {
		var tempX = x
		val negative = tempX < 0
		if (negative) {
			tempX = -tempX
		}

		var degrees = tempX.toInt()
		tempX = 60.0 * (tempX - degrees)
		val minutes = tempX.toInt()
		tempX = 60.0 * (tempX - minutes)
		var seconds =
			(10.0 * tempX).roundToInt() / 10.0   // Round to the nearest tenth of an arcsecond.

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

	private fun dmsToTime( d: Int, m: Int, s: Int, tRotation: Int ): PT {

		val totalDegrees = d + m / 60f + s / 3600f
		val rotationFraction = totalDegrees / 360f
		val timeInSeconds = ( rotationFraction * tRotation )
		val days = 0f
		val hours = (timeInSeconds / 3600f)
		val minutes = ((timeInSeconds % 3600f) / 60f)
		val seconds = (timeInSeconds % 60f)

		// Log.d("Calculations", "dmsToTime():   $d, $m, $s, $totalDegrees, $rotationFraction, $timeInSeconds")
		// Log.d("Calculations", "dmsToTime():   $days, $hours, $minutes, $seconds")

		return PT( days.toInt(), hours.toInt(), minutes.toInt(), seconds.toInt(), timeInSeconds.toInt() )
	}

	private fun calculateDayValue(): Double {
		val now = Calendar.getInstance().timeInMillis

		// Create a calendar instance for January 1, 2000
		val year2000 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
			set(Calendar.YEAR, 2000)
			set(Calendar.MONTH, 0) // Months are 0-based in Calendar
			set(Calendar.DAY_OF_MONTH, 1)
			set(Calendar.HOUR_OF_DAY, 0)
			set(Calendar.MINUTE, 0)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}

		// Calculate the difference in days
		return 1.0 + (now - year2000.timeInMillis) / (3600.0 * 24.0 * 1000.0)
	}

	fun getCompassDirection( observer: Observer, time: ZonedDateTime, body: Body): Int {
		Log.d("Calculations", "getCompassDirection()")
		return 0
	}

	fun update() {

		// val filterUpdate = false
		val timeA = Time.fromMillisecondsSince1970(Calendar.getInstance().timeInMillis)

		// if (filterUpdate) {
		// 	// update one planet only
		// 	// Log.d("Calculations", "update() $WatchFaceSettingsState")
		// 	val name = "URANUS"

		// 	// got local coordinates?
		// 	if (this.latLonElev.first == 0.0 && this.latLonElev.second == 0.0 && this.latLonElev.third == 0.0) return
		// 	// a current body exists?
		// 	val currentBody = getBodyFromThemeName( name )
		// 	Log.d("Calculations","---- $currentBody")
		// 	if ( currentBody == null ) return

		// 	val equatorial = equator( currentBody, timeA, observer, EquatorEpoch.OfDate, Aberration.Corrected);
		// 	val horizontal = horizon(timeA, observer, equatorial.ra, equatorial.dec, Refraction.Normal);

		// 	var convertedRa: DMS;
		// 	convertedRa = if (currentBody != Body.Earth) {
		// 		convertToDMS(equatorial.ra)
		// 	} else {
		// 		val localTime = Calendar.getInstance()
		// 		DMS(
		// 			localTime.get(Calendar.HOUR_OF_DAY),
		// 			localTime.get(Calendar.MINUTE),
		// 			localTime.get(Calendar.SECOND).toDouble(),
		// 			false
		// 		)
		// 	}
		// 	val convertedDec = convertToDMS(equatorial.dec);
		// 	val totalRotationTimeHours = getReferenceDataFromThemeName(currentBody.name)?.totalRotationTimeHours?: 0
		// 	val time = dmsToTime( convertedRa.degrees, convertedRa.minutes, convertedRa.seconds.toInt(), totalRotationTimeHours.toInt() )
		// 	bodyDataMap[currentBody] = Data(
		// 		equatorial,
		// 		horizontal,
		// 		convertedRa,
		// 		convertedDec,
		// 		time,
		// 		elapsedSeconds = 0
		// 	)

		// } else {

			// update all bodies

			bodyList.forEach {

				val observer = if (it.name=== "Earth") Observer( latLonElev.first , latLonElev.second, latLonElev.third )
					else Observer( latLonElevNull.first , latLonElevNull.second, latLonElevNull.third )
				val equatorial = equator(it, timeA, observer, EquatorEpoch.OfDate, Aberration.Corrected)
				val horizontal = horizon(timeA, observer, equatorial.ra, equatorial.dec, Refraction.Normal)

				var convertedRa: DMS = if (it != Body.Earth) {
					convertToDMS(equatorial.ra)
				} else {
					// Get hours, minutes, seconds from local time
					val localTime = Calendar.getInstance()
					DMS(
						localTime.get(Calendar.HOUR_OF_DAY),
						localTime.get(Calendar.MINUTE),
						localTime.get(Calendar.SECOND).toDouble(),
						false
					)
				}
				val convertedDec = convertToDMS(equatorial.dec);

				//

				val totalRotationTimeHours = getReferenceDataFromThemeName( it.name )!!.totalRotationTimeHours
				val totalRotationTimeSeconds = totalRotationTimeHours.toFloat() * 3600
				val elapsedAngle = equatorial.ra
				val elapsedFraction = elapsedAngle / 360
				val elapsedHours = elapsedFraction * totalRotationTimeHours
				val (hh,mm,ss) = convertToPlanetTime( equatorial.ra, elapsedHours.toFloat() )
				val time = PT( 0, hh, mm, ss, elapsedHours.toInt() )

				val rotationDirection = getCompassDirection(
					Observer( latLonElev.first , latLonElev.second, latLonElev.third ),
					ZonedDateTime.now(),
					it
				)

				//
				//
				//

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
				// Log.d("Calc","${it.name}\t\t\tRA ${equatorial.ra} \t\t ${horizontal.ra} \n\t\t\t\ttotal ${totalRotationTimeHours} \t\t elapsed ${elapsedHours}")
				// Log.d("Calc","${it.name}\t\t\t\t total ${totalRotationTimeHours}\t elapsed ${elapsedHours}")
				bodyDataMap[it] = Data(
					equatorial,
					horizontal,
					convertedRa,
					convertedDec,
					time,
					totalRotationTimeHours.toInt(),
                    rotationDirection
				)
			}

		// }
	}

	fun calculateRASeconds(raDegrees: Float, totalRotationTimeSeconds: Int): Float {
		return (raDegrees / 360f) * totalRotationTimeSeconds
	}

	fun convertToPlanetTime( ra: Double, totalRotationTimeHours: Float): Triple<Int, Int, Int> {

		val elapsedSeconds = calculateRASeconds( ra.toFloat(), (totalRotationTimeHours * 3600).toInt() )
    	val elapsedRotationTimeHours = elapsedSeconds / 3600
		// Log.d("Calc","RA ${ra} \t\t—— ${totalRotationTimeHours} \t\t—— ${elapsedRotationTimeHours}")

		val hours = (elapsedRotationTimeHours % totalRotationTimeHours).toInt()
		val minutes = ((elapsedSeconds % 3600) / 60)
		val seconds = (elapsedSeconds % 60)

		// Log.d("Calc", "\t\t\t\tTime ${hours}:${minutes}:${seconds} —— ${totalRotationTimeHours}")
    	return Triple(hours.toInt(), minutes.toInt(), seconds.toInt())
	}

}
