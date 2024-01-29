package jp.lab75.galaxytime.calculations;

import io.github.cosinekitty.astronomy.Equatorial;
import io.github.cosinekitty.astronomy.Topocentric;



public class Data  {

	Equatorial equatorial;
	Topocentric horizontal;

	// for getting time in hours, minutes, seconds
	DMS rightAscension;

	// for getting time in degrees, minutes, seconds of arc
	DMS declination;
}
