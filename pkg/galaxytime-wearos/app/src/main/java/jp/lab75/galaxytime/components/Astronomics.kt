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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Astronomics(
	modifier: Modifier = Modifier.fillMaxSize(),
	canvasSize: Dp = 450.dp,
	color: Color = Color(0xffffffff), // MaterialTheme.colors.surface,
	degrees: Int = 360,
	callback:  () -> Unit = {}
) {


}
