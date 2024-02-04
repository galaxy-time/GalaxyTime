package jp.lab75.galaxytime.calculations

import android.Manifest
import android.app.Activity

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

import jp.lab75.galaxytime.views.MainActivity
import kotlin.math.roundToInt

class Calculations(private val context: Context) {
	private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
	private val bodyDataMap = mutableMapOf<Body, Data>()
	private var latLonElev = Triple(0.0, 0.0, 0.0)

	val bodyList = arrayOf(
		Body.Earth, Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars,
		Body.Jupiter, Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
	)

	// Function to get data for calculated bodies
	fun getBodyData(body: Body): Data? {
		if (!bodyDataMap.containsKey(body)) {
			return null
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
			println("No permissions")
			return
		}

		fusedLocationClient.getCurrentLocation(
			CurrentLocationRequest.Builder().setDurationMillis(10000)
				.setMaxUpdateAgeMillis(10000)
				.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
				.build(), null
		).addOnSuccessListener { location: Location? ->

			println("Update location $location");

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

		// Crete calculations for each body
		bodyList.forEach {
			val equatorial =
				equator(it, timeA, observer, EquatorEpoch.OfDate, Aberration.Corrected);
			val horizontal =
				horizon(timeA, observer, equatorial.ra, equatorial.dec, Refraction.Normal);
			println("------------------------------------------------------------")
			println("Body ${it.name}")
			println("Azimuth ${horizontal.azimuth}")
			println("Altitude ${horizontal.altitude}")
			println("RA ${equatorial.ra}")
			// Convert RA to hh mm ss
			val convertedRa = convertToDMS(equatorial.ra);
			println("RA: ${convertedRa.degrees}h ${convertedRa.minutes}m ${convertedRa.seconds}s ${convertedRa.negative}")

			// Convert Dec to
			val convertedDec = convertToDMS(equatorial.dec);
			println("Dec: ${if (convertedDec.negative) "-" else ""}${convertedDec.degrees}° ${convertedDec.minutes}' ${convertedDec.seconds}\" ${convertedDec.negative}")


			// Write data to map or update existing data
			bodyDataMap[it] = Data(equatorial, horizontal, convertedRa, convertedDec)
		}
	};
}
