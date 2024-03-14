package jp.lab75.galaxytime.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun getRotation(degrees: Int, lastRotation: Int): Int {
    var newRotation = lastRotation // newRotation will be updated in proper way
    // last rotation converted to range [-359; 359]
    val modLast = if (lastRotation > 0) lastRotation % 360 else 360 - (-lastRotation % 360)

    // if modLast isn't equal rotation retrieved as function argument
    // it means that newRotation has to be updated

    if (modLast != degrees) {
        // distance in degrees between modLast and rotation going backward
        val backward = if (degrees > modLast) modLast + 360 - degrees else modLast - degrees
        // distance in degrees between modLast and rotation going forward
        val forward = if (degrees > modLast) degrees - modLast else 360 - modLast + degrees

        // update newRotation so it will change rotation in the shortest way
        newRotation = if (backward < forward) {

            // backward rotation is shorter
            lastRotation - backward
        } else {

            // forward rotation is shorter (or they are equal)
            lastRotation + forward
        }
    }
    return newRotation
}

@Composable
fun BaseCompass(
	degrees: Int = 360,
	variantWidget: @Composable (rotationAngle: Float) -> Unit
) {

    val (lastRotation, setLastRotation) = remember { mutableStateOf(0) }
    val newRotation = getRotation(degrees, lastRotation)
    setLastRotation(newRotation)
    val targetRotation = -( newRotation - 0 )

    val rot by animateFloatAsState(
        targetValue = targetRotation.toFloat(),
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOut
        ), label = ""
	)

    variantWidget( rot )

}
