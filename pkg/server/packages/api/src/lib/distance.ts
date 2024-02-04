// Distance

// The haversine formula determines the great-circle distance
// between two points on a sphere given their longitudes and latitudes.
// Important in navigation, it is a special case of a more general formula
// in spherical trigonometry, the law of haversines,
// that relates the sides and angles of spherical triangles.
// sources:
// https://en.wikipedia.org/wiki/Haversine_formula
// https://www.movable-type.co.uk/scripts/latlong.html

// Haversine formula:
// 	a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
// 	c = 2 ⋅ atan2( √a, √(1−a) )
// 	d = R ⋅ c
// 	where φ is latitude, λ is longitude, R is earth’s radius (mean radius = 6,371km);
// 	note that angles need to be in radians to pass to trig functions!

const haversine = (
	lat1: number,
	lon1: number,
	lat2: number,
	lon2: number
): number => {

	const R = 6371e3; // metres
	const φ1 = lat1 * Math.PI/180; // φ, λ in radians
	const φ2 = lat2 * Math.PI/180;
	const Δφ = (lat2-lat1) * Math.PI/180;
	const Δλ = (lon2-lon1) * Math.PI/180;

	const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
			Math.cos(φ1) * Math.cos(φ2) *
			Math.sin(Δλ/2) * Math.sin(Δλ/2);
	const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

	const d = R * c; // in metres

	return d;
}
