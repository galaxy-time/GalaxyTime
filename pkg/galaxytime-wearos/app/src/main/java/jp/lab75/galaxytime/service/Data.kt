package jp.lab75.galaxytime.service

import io.github.cosinekitty.astronomy.AxisInfo
import io.github.cosinekitty.astronomy.Equatorial
import io.github.cosinekitty.astronomy.Topocentric

data class Data(
	var equatorial: Equatorial,
	var horizontal: Topocentric,
	var rightAscension: DMS,
	var declination: DMS,
	var rotation: AxisInfo,
	var totalSolarDays: Double,
	var solarDay: Int = 0,
	var distance: Double,
	var dayOfYear: Double,
)
