package jp.lab75.galaxytime.service

import android.Manifest
import android.util.Log

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import java.util.Calendar
import java.util.TimeZone

import kotlin.math.roundToInt

class Calculations(private val context: Context) {

	private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
	private val bodyDataMap = mutableMapOf<Body, Data>()
	private var latLonElev = Triple(0.0, 0.0, 0.0)

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

		val totalDegrees = d + m / 60 + s / 3600
		val rotationFraction = totalDegrees / 360
		val timeInSeconds = ( rotationFraction * tRotation )

		Log.d("Calculations", "$totalDegrees, $rotationFraction, $timeInSeconds")

		val days = 0
		val hours = (timeInSeconds / 3600).toInt()
		val minutes = ((timeInSeconds % 3600) / 60).toInt()
		val seconds = (timeInSeconds % 60).toInt()

		return PT( days, hours, minutes, seconds )
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

	fun update() {
		if (this.latLonElev.first == 0.0 && this.latLonElev.second == 0.0 && this.latLonElev.third == 0.0) {
			// Not ready yet, we first need the location to update data
			return;
		}

		val timeA = Time.fromMillisecondsSince1970(Calendar.getInstance().timeInMillis);
		val observer = Observer(
			latLonElev.first,
			latLonElev.second,
			latLonElev.third
		)

		// Create calculations for each body
		bodyList.forEach {
			val equatorial = equator(it, timeA, observer, EquatorEpoch.OfDate, Aberration.Corrected);
			val horizontal = horizon(timeA, observer, equatorial.ra, equatorial.dec, Refraction.Normal);
			Log.d("Calculations","------------------------------------------------------------")
			Log.d("Calculations","Body       ${it.name}")
			Log.d("Calculations","RA         ${equatorial.ra}")
			Log.d("Calculations","DEC        ${equatorial.dec}")
			// Log.d("Calculations","horizontal ${horizontal.ra}")
			// Log.d("Calculations","Azimuth    ${horizontal.azimuth}")
			// Log.d("Calculations","Altitude   ${horizontal.altitude}")

			// Convert RA to hh mm ss
			var convertedRa: DMS;

			convertedRa = if (it != Body.Earth) {
				convertToDMS(equatorial.ra);
			} else {
				// Get hours, minutes, seconds from local time
				val localTime = Calendar.getInstance()
				DMS(
					localTime.get(Calendar.HOUR_OF_DAY),
					localTime.get(Calendar.MINUTE),
					localTime.get(Calendar.SECOND).toDouble(),
					false
				);
			}

			// Log.d("Calculations","RA: ${convertedRa.degrees}h ${convertedRa.minutes}m ${convertedRa.seconds}s ${convertedRa.negative}")

			// Convert Dec to
			val convertedDec = convertToDMS(equatorial.dec);
			// Log.d("Calculations","Dec: ${if (convertedDec.negative) "-" else ""}${convertedDec.degrees}° ${convertedDec.minutes}' ${convertedDec.seconds}\" ${convertedDec.negative}")

			// dd:hh:mm:ss
			val rotationTime = getReferenceDataFromThemeName(it.name)?.rotationTime?: 0
			val time = dmsToTime( convertedRa.degrees, convertedRa.minutes, convertedRa.seconds.toInt(), rotationTime )
			Log.d("Calculations","Localtime: ${time.d}:${time.h}:${time.m}:${time.s}")

			// Write data to map or update existing data
			bodyDataMap[it] = Data( equatorial, horizontal, convertedRa, convertedDec, time )
		}
	};
}
