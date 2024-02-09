package jp.lab75.galaxytime.service

data class CelestialBody(
    val name: String,
    val radiusKm: Double,
    val surfaceAreaKm2: Double,
    val distanceAU: Double,
    val rotationTime: Int
)

val celestialBodies = arrayOf(
    CelestialBody("Mercury",  2439.7,    74800000.0,  0.39,  5067000),
    CelestialBody("Venus",    6051.8,   460200000.0,  0.72, 20996760),
    CelestialBody("Earth",    6371.0,   510100000.0,  1.00,    86400),
    CelestialBody("Mars",     3389.5,   144800000.0,  1.52,    88775),
    CelestialBody("Jupiter", 69911.0, 61420000000.0,  5.20,    35730),
    CelestialBody("Saturn",  58232.0, 42700000000.0,  9.58,    38140),
    CelestialBody("Uranus",  25362.0,  8113000000.0, 19.22,    62040),
    CelestialBody("Neptune", 24622.0,  7618000000.0, 30.05,    57960),
    CelestialBody("Moon",     1737.4,    37900000.0,  1.00,  2360520),
    CelestialBody("Io",       1821.6,    41790000.0,  5.20,   152853),
    CelestialBody("Europa",   1560.8,    30620000.0,  5.20,   306720),
    CelestialBody("Ganymede", 2634.1,    87200000.0,  5.20,   357000),
    CelestialBody("Callisto", 2410.3,    73000000.0,  5.20,   400392),
    CelestialBody("Titan",    2574.7,    83380000.0,  9.58,  1389600),
    CelestialBody("Titania",   788.9,     7850000.0, 19.22,  2094000),
    CelestialBody("Triton",   1353.4,    23070000.0, 30.05,   -57960)
)

fun getReferenceDataFromThemeName(themeName: String): CelestialBody? {
	return celestialBodies.find { it.name.lowercase() == themeName.lowercase() }
}
