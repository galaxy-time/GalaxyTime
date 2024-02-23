package jp.lab75.galaxytime.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
// import androidx.wear.compose.ui.text.font.FontStyle
// import androidx.wear.compose.ui.text.font.FontWeight

import jp.lab75.galaxytime.R

private val appFont = FontFamily(
	Font(R.font.b612),
)

val Typography = Typography(
	defaultFontFamily = appFont,
)

@Composable
fun GalaxyTimeTheme(
	content: @Composable () -> Unit
) {
	/**
	 * Empty theme to customize for your app.
	 * See: https://developer.android.com/jetpack/compose/designsystems/custom
	 */
	MaterialTheme(
		typography = Typography,
		content = content
	)
}
