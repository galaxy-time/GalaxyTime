import express from "express"
import log from "@lab75/logger"

import {readFileSync} from 'fs'
import dotenv from 'dotenv'
import pkg from '../package.json' assert {type: "json"}

import {ApolloServer} from '@apollo/server'
import {startStandaloneServer} from "@apollo/server/standalone"
import resolvers from './graph/resolvers.js'

log('BOOTING...')
log('🪐 SPACETIME', pkg.name, pkg.version)

// GRAPHQL SERVER

const typeDefs = readFileSync('./src/graph/schema.graphql', {encoding: 'utf-8'})
const server = new ApolloServer({typeDefs, resolvers})
const {url} = await startStandaloneServer(server, {listen: {port: 4000}})
log(`🪐 GRAPH API listening on ${url}`)

// REST SERVER

const app = express()
app.use((req, res) => {
	log("Received request for URL:", req.url)
	res.end("Hello from SpaceTime!")
})
app.listen(3000, () => {
	log("🪐 REST API server listening on http://localhost:3000/")
})


import Astronomy from 'astronomy-engine';

const observer = new Astronomy.Observer(48.3668041, 10.836724, 526.4);

// Snacked from Astronomy.js
function DMS(x) {
	let a: any = {};

	a.negative = (x < 0);
	if (a.negative) {
		x = -x;
	}

	a.degrees = Math.floor(x);
	x = 60.0 * (x - a.degrees);
	a.minutes = Math.floor(x);
	x = 60.0 * (x - a.minutes);
	a.seconds = Math.round(10.0 * x) / 10.0;   // Round to the nearest tenth of an arcsecond.

	if (a.seconds == 60) {
		a.seconds = 0;
		if (++a.minutes == 60) {
			a.minutes = 0;
			++a.degrees;
		}
	}

	return a;
}

// Snacked from Demo html page
function HtmlRightAscension(ra) {
	var dms = DMS(ra);
	if (dms.negative) {
		throw "Encountered negative right ascension!  " + ra;
	}

	var hours = (dms.degrees < 10 ? "0" : "") + dms.degrees.toString();
	var minutes = (dms.minutes < 10 ? "0" : "") + dms.minutes.toString();
	var seconds = (dms.seconds < 10 ? "0" : "") + dms.seconds.toFixed(1);
	var s = hours + "<sup class='UnitSup'>h</sup>&nbsp;" + minutes + "<sup class='UnitSup'>m</sup>&nbsp;" + seconds + "<sup class='UnitSup'>s</sup>";
	return s;

}

function HtmlDeclination(dec) {
	var dms = DMS(dec);
	var s = dms.negative ? "&minus;" : "&nbsp;";

	if (dms.degrees < 100) {
		s += "0";
	}

	var hours = (dms.degrees < 10 ? "0" : "") + dms.degrees.toString();
	var minutes = (dms.minutes < 10 ? "0" : "") + dms.minutes.toString();
	var seconds = (dms.seconds < 10 ? "0" : "") + dms.seconds.toFixed(1);
	s += hours + "&deg;&nbsp;" + minutes + "'&nbsp;" + seconds + "&quot;";
	return s;

}

const bodies = [
	'Sun', 'Moon', 'Mercury', 'Venus', 'Mars',
	'Jupiter', 'Saturn', 'Uranus', 'Neptune', 'Pluto'
];

function toHMS(decimalHours) {
	const hours = Math.floor(decimalHours);
	const minutes = Math.floor((decimalHours - hours) * 60);
	const seconds = ((decimalHours - hours - minutes / 60) * 3600).toFixed(1);
	return `${hours}h ${minutes}m ${seconds}s`;
}

function degreesToSiderealHours(degrees) {
	return degrees / 15;
}


// Function to calculate and log positions
function logPositions() {
	// Use the current date and time
	let now = new Date();

	// Snaked from Astronomy.js function (DayValue)
	const day = 1.0 + (now.getTime() - Date.UTC(2000, 0, 1)) / (3600.0 * 24.0 * 1000.0)

	const axx = 1.0 + (now.getTime() - Date.UTC(2000, 0, 1));
	const xyy = (3600.0 * 24.0 * 1000.0)

	// Calculate and log the position for each body
	bodies.forEach(body => {
		let equatorial = Astronomy.Equator(body as any, now, observer, true, true);
		let horizontal = Astronomy.Horizon(now, observer, equatorial.ra, equatorial.dec, 'normal');
		let raHMS = toHMS(degreesToSiderealHours(equatorial.ra));

		console.log(`${body}:`);
		console.log(equatorial, horizontal);
		console.log(`  RA (Right Ascension): ${raHMS}`);
		console.log(`  DEC (Declination): ${equatorial.dec.toFixed(2)} degrees`);
		console.log(`  Azimuth: ${horizontal.azimuth.toFixed(2)} degrees`);
		console.log(`  Altitude: ${horizontal.altitude.toFixed(2)} degrees`);
		console.log();
	});
}

// Call the function
logPositions();


import {AstronomyClass, GeographicCoordinates, ConstellationByConciseName} from './astronomy.js'

const astronomyClass = new AstronomyClass();

// Get day value
const day = astronomyClass.DayValue(new Date());

const PRECISION = 7;


astronomyClass.Body.forEach((body: any) => {
	const distance = body.DistanceFromEarth(day);
	const pc = body.GeocentricCoordinates(day)
	const location = new GeographicCoordinates(48.3668041, 10.836724, 526.4);
	let magnitude = "";
	let sunAngle = "";
	let constellation = "";
	let ra = "";
	let dec = "";

	if (body.Name !== 'Earth') {
		let eq = body.EquatorialCoordinates(day, location);
		ra = HtmlRightAscension(eq.longitude);
		dec = HtmlDeclination(eq.latitude);
		sunAngle = astronomyClass.AngleWithSunInDegrees(body,day).toFixed(1) + " deg"
		magnitude = body.VisualMagnitude(day).toFixed(2);
		constellation = astronomyClass.FindConstellation(eq)?.FullName ?? "";
	}

	console.log(`_name, ${body.Name}:`);
	console.log(`Distance from Earth: ${distance}`);
	console.log(`_x:`,
		pc.x.toFixed(PRECISION),
		`_y:`,
		pc.y.toFixed(PRECISION),
		`_z:`,
		pc.z.toFixed(PRECISION)
	);
	console.log(`_mag: ${magnitude}`);
	console.log(`_sunAngle: ${sunAngle}`);
	console.log(`_constellation: ${constellation}`);
	console.log(`Equatorial Coordinates: ${ra} ${dec}`);
	console.log();
});



