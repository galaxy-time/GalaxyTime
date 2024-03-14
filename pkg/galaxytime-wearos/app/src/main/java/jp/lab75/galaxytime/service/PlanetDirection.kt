// return the direction of a planet based on the current date, time and geo location on earth

import io.github.cosinekitty.astronomy.*
import java.time.ZonedDateTime

private val bodyList = arrayOf(
	Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars,
	Body.Jupiter, Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
)

internal fun `Celestial body positions demo`(observer: Observer, time: Time, body: Body): Int {

	println("UTC date = $time")
	println()
	println("BODY           RA      DEC       AZ      ALT")
	for (body in bodyList) {
		val equ_2000: Equatorial = equator(body, time, observer, EquatorEpoch.J2000, Aberration.Corrected)
		val equ_ofdate: Equatorial = equator(body, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)
		val hor: Topocentric = horizon(time, observer, equ_ofdate.ra, equ_ofdate.dec, Refraction.Normal)
		println("%-8s %8.2f %8.2f %8.2f %8.2f".format(body, equ_2000.ra, equ_2000.dec, hor.azimuth, hor.altitude))
	}
	return 0
}

//fun findPlanetDirection(latitude: Double, longitude: Double, dateTime: ZonedDateTime, planet: Body): String {
//
//	val astronomy = Astronomy()
//
//    // Create an observer object with the provided latitude and longitude
//    val observer = Observer(latitude, longitude)
//
//    // Convert the provided ZonedDateTime to the format expected by the library
//    val time = Time.fromZonedDateTime(dateTime)
//
//    // Calculate the topocentric position of the planet for the given time and observer location
//    val equatorial = Astronomy.Equator(planet, time, observer, true, true)
//
//    // Convert equatorial coordinates to horizontal coordinates to get the direction (azimuth)
//    val horizontal = Astronomy.Horizon(time, observer, equatorial.ra, equatorial.dec, Refraction.Atmospheric)
//
//    // Format the direction for output
//    return "Azimuth: ${horizontal.azimuth}, Altitude: ${horizontal.altitude}"
//}
//
//// Example usage
//fun main() {
//    val latitude = 40.7128 // New York City latitude
//    val longitude = -74.0060 // New York City longitude
//    val dateTime = ZonedDateTime.of(2024, 2, 22, 20, 0, 0, 0, ZoneId.of("America/New_York"))
//    val planetDirection = findPlanetDirection(latitude, longitude, dateTime, Body.Jupiter)
//
//    println(planetDirection)
//}
