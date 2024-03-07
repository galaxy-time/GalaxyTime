package jp.lab75.galaxytime.service

data class CelestialBody(
    val name: String = "",
    val radiusKm: Double = 0.0,
    val surfaceAreaKm2: Double = 0.0,
    val distanceAU: Double = 0.0,
    val totalRotationTimeHours: Float = 0f,
	val radius: Float = 0f,
	val satellites: Int = 0,
)

val celestialBodies = arrayOf(
    CelestialBody("Sun",    695700.0, 6082104402130.0,  0.00,  600.0f, 30f),
    CelestialBody("Mercury",  2439.7,      74800000.0,  0.39, 4222.6f,  4f),
    CelestialBody("Venus",    6051.8,     460200000.0,  0.72, 2802.0f,  5f),
    CelestialBody("Earth",    6371.0,     510100000.0,  1.00,   24.0f,  5f, 1),
    CelestialBody("Moon",     1737.4,      37900000.0,  1.00,  708.7f,  3f),
    CelestialBody("Mars",     3389.5,     144800000.0,  1.52,   24.7f,  4f, 2),
    CelestialBody("Jupiter", 69911.0,   61420000000.0,  5.20,    9.9f, 12f, 95),
    CelestialBody("Saturn",  58232.0,   42700000000.0,  9.58,   10.7f, 12f, 146),
    CelestialBody("Uranus",  25362.0,    8113000000.0, 19.22,   17.2f,  8f, 28),
    CelestialBody("Neptune", 24622.0,    7618000000.0, 30.05,   16.1f,  8f, 16),
	CelestialBody("Pluto",    1188.3,      17744430.0, 39.50,  153.3f,  3f, 5),
)

fun getReferenceDataFromThemeName(themeName: String): CelestialBody {
	return celestialBodies.find { it.name.lowercase() == themeName.lowercase() } ?: celestialBodies[0]
}
