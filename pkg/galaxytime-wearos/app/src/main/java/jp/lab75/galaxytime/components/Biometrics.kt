package jp.lab75.galaxytime.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Biometrics(
	modifier: Modifier = Modifier.fillMaxSize(),
	canvasSize: Dp = 450.dp,
	color: Color = Color(0xffffffff), // MaterialTheme.colors.surface,
	degrees: Int = 360,
	callback:  () -> Unit = {}
) {

//    val ( lastHydrationTime, setLastHydrationTime ) = remember { mutableStateOf(0) }
//	val ( totalHydration, setTotalHydration ) = remember { mutableStateOf(0) }
//	val ( currentHydration, setCurrentHydration ) = remember { mutableStateOf(0) }

}

fun DrawScope.biometricsGraphics(w: Float, h: Float, m: Float, d: Float, c: Color ){

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
