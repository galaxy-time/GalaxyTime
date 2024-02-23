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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import jp.lab75.galaxytime.WatchFaceCanvasRenderer

import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.theme.GalaxyTimeTheme
import kotlin.math.cos
import kotlin.math.sin

fun degreeToDirection(degrees: Int): String {
	return when (degrees) {
        in (0..22) -> "N"
		in (23..67) -> "NE"
		in (68..112) -> "E"
		in (113..157) -> "SE"
		in (158..202) -> "S"
		in (203..247) -> "SW"
		in (248..292) -> "W"
		in (293..337) -> "NW"
		in (338..360) -> "N"
		else -> ""
    }
}

//fun handleTap(context: Context) {
//
//	val wf = ComponentName(context, WatchFaceCanvasRenderer::class.java)
//	val intent: Intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
//		.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, wf)
//		.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//	context.startActivity(intent)
////	finish()
//}

@Composable
fun MinimalCompass(
	modifier: Modifier = Modifier.fillMaxSize(),
	canvasSize: Dp = 450.dp,
	color: Color = Color(0xffffffff), // MaterialTheme.colors.surface,
	degrees: Int = 360,
	callback:  () -> Unit = {}
) {

	val margin = 20f
	val density = LocalDensity.current;
	val configuration = LocalConfiguration.current;
	val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
	val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

	BaseCompass(
        degrees = degrees
    ) { rot ->
		Box {
			Image(
				painter = painterResource(R.drawable.sgt_planet_compass),
				"image",
				Modifier
				.fillMaxSize()
//				.rotate(rot)
				.graphicsLayer {
					rotationZ = rot
				},
				contentScale = ContentScale.Fit
			)
			Box(
				modifier = Modifier
				.rotate(rot)
				.graphicsLayer {
					rotationZ = rot
				}
				.fillMaxSize()
				.drawBehind {
					compassArrow(
						w = screenWidthPx.toFloat(),
						h = screenHeightPx.toFloat(),
						m = margin,
						d = rot,
						c = color
						)
					}
			)

			Box(
				modifier = Modifier
				.fillMaxSize()
				.clickable(true) { callback() },
				contentAlignment = Alignment.Center
			){
				Text(
					modifier = Modifier.fillMaxWidth(),
					textAlign = TextAlign.Center,
					color = color,
					fontSize = 10.sp,
					text = "${degrees}˚ ${degreeToDirection(degrees)}",
				)
			}
			// grain layer
//			Image(
//				painter = painterResource(R.drawable.sgt_grain),
//				"image",
//				Modifier
//					.fillMaxSize()
//					.rotate(rot),
//				contentScale = ContentScale.Fit
//			)
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
@Preview(showBackground = true)
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
