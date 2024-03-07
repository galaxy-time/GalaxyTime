package jp.lab75.galaxytime.views

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import io.github.cosinekitty.astronomy.Body
import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.WatchFaceCanvasRenderer
import jp.lab75.galaxytime.data.watchface.WatchFaceColorPalette.Companion.convertToColorPalette
import jp.lab75.galaxytime.data.watchface.WatchFaceData
import jp.lab75.galaxytime.service.Calculations
import jp.lab75.galaxytime.service.CelestialBody
import jp.lab75.galaxytime.service.Data
import jp.lab75.galaxytime.service.getReferenceDataFromThemeName
import jp.lab75.galaxytime.settings.WatchFaceSettingsActivity
import jp.lab75.galaxytime.settings.WatchFaceSettingsState
import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import kotlin.math.roundToInt

class AstronomicsActivity : ComponentActivity() {

	lateinit var context: Context
	lateinit var calculations: Calculations

//	lateinit var body: Body
//	lateinit var data: Data

	lateinit var ref: CelestialBody
	lateinit var sunRef: CelestialBody

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setTheme(android.R.style.Theme_DeviceDefault)

		context = applicationContext
		calculations = Calculations.getInstance(context)

		// get config
		val bun = intent.extras
		val name = (bun?.getString("name") ?: "unknown")
		val c = bun?.getInt("color") ?: "0xffff00ff"
		val color = Color( c as Int )

//		val body = calculations.getCurrentBody()
//		val data = calculations.getCurrentBodyData()
		ref = getReferenceDataFromThemeName( name )
		sunRef = getReferenceDataFromThemeName("SUN")
//		Log.d(TAG,"${body}\n${data}")

		setContent {
			GalaxyTimeTheme {
				AstronomicsApp(
					callback = { finishActivity() },
					name = name,
					color = color,
					calculations = calculations,
					ref = ref,
					sunRadius = sunRef.radius,
				)
			}
		}

	}

    override fun onPause() {
        super.onPause()
		Log.d(TAG, "onPause()")
    }

 	override fun onResume() {
        super.onResume()
		Log.d(TAG, "onResume()")
    }

	fun finishActivity() {
		Log.d(TAG,"finishActivity()")
		finish()
	}

	companion object {
		const val TAG = "Astronomics"
	}

}

@Composable
fun AstronomicsApp(
	callback: () -> Unit,
	name: String,
	color: Color,
	calculations: Calculations,
	ref: CelestialBody,
	sunRadius: Float
) {

	val margin = 20f
	val density = LocalDensity.current;
	val configuration = LocalConfiguration.current;
	val width = with(density) { configuration.screenWidthDp.dp.roundToPx() }
	val height = with(density) { configuration.screenHeightDp.dp.roundToPx() }
	val offset = height / 2 + 30

	Log.d("Astronomics Composable", "${name} ${color} ${ref}")

	Box( modifier = Modifier
		.fillMaxSize()
		.clickable(true) { callback() }
	) {
//		if ( data != null ) {
			AstronomicsView(
				name = name,
				color = color,
				calculations = calculations,
				ref = ref,
				sunRadius = sunRadius,
			)
//		} else {
//			Text(
//				textAlign = TextAlign.Center,
//				color = Color(0xffff0000),
//				fontSize = 10.sp,
//				text = "LOST IN SPACE",
//				modifier = Modifier
//					.fillMaxWidth()
//					.padding(10.dp)
//					.align(alignment = Alignment.Center)
//			)
//		}
		Box(
			modifier = Modifier
				.fillMaxSize()
				.drawBehind {
					drawRect(
						color = color,
						size = size,
						blendMode = BlendMode.Overlay
					)
				}

		)
	}

}

@Composable
fun AstronomicsView(
	name: String,
	color: Color,
	calculations: Calculations,
	ref: CelestialBody,
	sunRadius: Float
) {

	val density = LocalDensity.current
	val configuration = LocalConfiguration.current
	val height = configuration.screenHeightDp.dp
	val yp = height / 2
	val off = -15.dp
	val off2 = 5.dp

	Image(
		painter = painterResource(R.drawable.sgt_planet_neutral),
		"image",
		Modifier.fillMaxSize(),
		contentScale = ContentScale.Fit
	)
	Box(
		modifier = Modifier
			.fillMaxSize()
			.drawBehind {
				drawRect(
					color = color,
					size = size,
					blendMode = BlendMode.Multiply
				)
			},
		contentAlignment = Alignment.Center
	) {

		DistanceGraph(name, ref, sunRadius)
		DescriptionText(name, ref, calculations)
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.offset(y = off),
			textAlign = TextAlign.Center,
			color = Color.White,
			text = "$name — SUN",
			fontSize = 10.sp
		)
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.offset(y = off2),
			textAlign = TextAlign.Center,
			color = Color.White,
			text = "${ref.distanceAU}AU",
			fontSize = 8.sp
		)
	}

}

@Composable
fun DescriptionText(
	name: String,
	ref: CelestialBody,
	calculations: Calculations
) {

	val data = calculations.getCurrentBodyData()
	Log.d("Astronomics Composable", "${data}")

	val l1 = "AZI ${data?.horizontal?.azimuth?.roundToInt()?.or(272)}° ALT ${data?.horizontal?.altitude?.roundToInt()?.or(-2)}°"

	val l2 = "RAS %02dD:%02dM:%02dS".format(
		data?.rightAscension?.degrees?.or(12),
		data?.rightAscension?.minutes?.or(14),
		data?.rightAscension?.seconds?.roundToInt()?.or(16)
	)
	val l3 = "DEC %02d˚%02d'%02d\"".format(
		data?.declination?.degrees?.or(12),
		data?.declination?.minutes?.or(14),
		data?.declination?.seconds?.roundToInt()?.or(16)
	)

	val leftColumn = arrayOf(l1,l2,l3).joinToString("\n")

	fun Float.round(decimals: Int = 2): Float = "%.${decimals}f".format(this).toFloat()

	val r1 = "\n\n\n\n\nSOL ${ref.totalRotationTimeHours}h"
	val r2 = "${(ref.totalRotationTimeHours / 24).round(2)} TERRAN DAYS"
	val r3 = "SURFACE ${ref.surfaceAreaKm2}km²"
	val r4 = if (ref.satellites>0) "SATELLITES ${ref.satellites}" else ""

	val rightColumn = arrayOf(r1,r2,r3,r4).joinToString("\n")

	Log.d("Astronomics Composable", leftColumn)
	Log.d("Astronomics Composable", rightColumn)

	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Text(
			modifier = Modifier.align(Alignment.Center),
			textAlign = TextAlign.Left,
			color = Color(0xaaffffff),
			text = "$leftColumn\n$rightColumn",
			fontSize = 8.sp,
			lineHeight = 10.sp,
		)
	}

}


@Composable
fun DistanceGraph(
	name: String,
	ref: CelestialBody,
	sunRadius: Float = 100f
) {

	val locationRadius: Float = ref.radius

	Box (
		modifier = Modifier
			.fillMaxSize()
			.drawBehind {

				val cx = size.width / 2f
				val cy = size.height / 2f
				val y = cy - 10f
				val off = size.width / 3f

				drawLine(
					start = Offset(cx - off + locationRadius, y),
					end = Offset(cx + off - sunRadius, y),
					color = Color.White,
					strokeWidth = 1f
				)
				drawCircle(
					color = Color.White,
					center = Offset(cx - off, y),
					radius = locationRadius,
					style = Stroke(width = 1f)
				)
				drawCircle(
					color = Color.White,
					center = Offset(cx + off, y),
					radius = sunRadius,
					style = Stroke(width = 1f)
				)

			}
	)
}

//
//
//

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun AstronomicsPreview() {

	val context = LocalContext.current
	val calculations = Calculations.getInstance( context )

	val name = "SATURN"
	val color = Color.Cyan
	val ref = getReferenceDataFromThemeName( name )

	GalaxyTimeTheme {
			AstronomicsView(
				name = name,
				color = color,
				ref = ref,
				calculations = calculations,
				sunRadius = 25f
			)
	}

}
