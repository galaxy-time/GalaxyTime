package jp.lab75.galaxytime.service;

import io.github.cosinekitty.astronomy.Equatorial;
import io.github.cosinekitty.astronomy.Topocentric;

data class Data(
	var equatorial: Equatorial,
	var horizontal: Topocentric,
	// for getting time in hours, minutes, seconds
	var rightAscension: DMS,
	// for getting time in degrees, minutes, seconds of arc
	var declination: DMS
)
