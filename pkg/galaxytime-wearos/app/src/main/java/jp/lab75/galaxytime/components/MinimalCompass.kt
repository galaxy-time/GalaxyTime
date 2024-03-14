package jp.lab75.galaxytime.components

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.Surface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.wear.tooling.preview.devices.WearDevices
import jp.lab75.galaxytime.WatchFaceCanvasRenderer

import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import kotlin.math.cos
import kotlin.math.sin

fun degreeToDirection(degrees: Int): String {
	return when (degrees) {
        in (0..22) -> "N"
		in (23..67) -> "NW"
		in (68..112) -> "W"
		in (113..157) -> "SW"
		in (158..202) -> "S"
		in (203..247) -> "SE"
		in (248..292) -> "E"
		in (293..337) -> "NE"
		in (338..360) -> "N"
		else -> ""
    }
}

@Composable
fun MinimalCompass(
	modifier: Modifier = Modifier.fillMaxSize(),
	canvasSize: Dp = 450.dp,
	color: Color = Color(0xffffffff), // MaterialTheme.colors.surface,
	degrees: Int = 360,
	azi: Double? = 0.0,
	callback:  () -> Unit = {}
) {

	val margin = 40f
	val density = LocalDensity.current
	val configuration = LocalConfiguration.current
	val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
	val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

	BaseCompass(
        degrees = degrees
    ) { rot ->

		val aziRot = if ( azi != null ) rot + azi.toInt() else rot

		Box {
			Image(
				painter = painterResource(R.drawable.sgt_planet_compass),
				"image",
				Modifier
				.fillMaxSize()
				.graphicsLayer {
					rotationZ = aziRot + 180
				},
				contentScale = ContentScale.Fit
			)
			Box(
				modifier = Modifier
					.graphicsLayer {
						rotationZ = rot
					}
					.fillMaxSize()
					.drawBehind {
						// north
						val w = screenWidthPx.toFloat()
						val h = screenHeightPx.toFloat()
						drawLine(
							strokeWidth = 1f,
							cap = StrokeCap.Round,
							color = Color.White,
							start = Offset( w * 0.5f, h * 0.1f ),
							end = Offset( w * 0.5f, h * 0.2f ),
							alpha = 0.5f
						)
					}
			) {
				Text(
					modifier = Modifier.fillMaxWidth().offset(y = 5.dp),
					textAlign = TextAlign.Center,
					fontFamily = FontFamily.Monospace,
					color = color,
					fontSize = 10.sp,
					text = "N",
				)
			}
			Box(
				modifier = Modifier
				.graphicsLayer {
					rotationZ = aziRot
				}
				.fillMaxSize()
				.drawBehind {
					compassArrow(
						w = screenWidthPx.toFloat(),
						h = screenHeightPx.toFloat(),
						m = margin,
						d = 0f, //rot?
						c = color
						)
					}
			)

			Box(
				modifier = Modifier
				.fillMaxSize()
				.clickable(true) { callback() },
//				contentAlignment = Alignment.Center
			)
//			{
//				Text(
//					modifier = Modifier.fillMaxWidth(),
//					textAlign = TextAlign.Center,
//					fontFamily = FontFamily.Monospace,
//					color = color,
//					fontSize = 10.sp,
//					text = "${degrees}˚ ${degreeToDirection(degrees)}",
//				)
//			}

		}
    }
}

fun DrawScope.compassArrow( w: Float, h: Float, m: Float, d: Float, c: Color ){

	drawLine(
        strokeWidth = 1f,
        cap = StrokeCap.Round,
		color = c,
		start = Offset( m, h - m ),
        end = Offset( w * 0.5f, m ),
		alpha = 0.9f
	)

	drawLine(
        strokeWidth = 1f,
        cap = StrokeCap.Round,
		color = c,
		start = Offset( w * 0.5f, m ),
        end = Offset( w - m, h - m ),
		alpha = 0.9f
	)

}

// fun DrawScope.compassBorder(
//     componentSize: Size,
//     color: Color
// ){
//     drawArc(
//         size = componentSize,
//         color = color,
//         startAngle = 0f,
//         sweepAngle = 360f,
//         useCenter = false,
//         style = Stroke(
//             width = size.width * 0.04f,
//             cap = StrokeCap.Round
//         ),

//         topLeft = Offset(
//             x = (size.width - componentSize.width) / 2f,
//             y = (size.height - componentSize.height) / 2f
//         )
//     )
// }

@Composable
@Preview(showBackground = true,device = WearDevices.SMALL_ROUND, showSystemUi = true)
fun MinimalCompassPreview() {
    GalaxyTimeTheme {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            MinimalCompass(degrees = 90)
        }
    }
}
