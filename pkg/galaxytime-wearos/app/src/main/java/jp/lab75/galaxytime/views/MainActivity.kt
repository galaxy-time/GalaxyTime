/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package jp.lab75.galaxytime.views

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.WatchFaceCanvasRenderer
import jp.lab75.galaxytime.theme.GalaxyTimeTheme


class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
//		installSplashScreen()
		super.onCreate(savedInstanceState)
		setTheme(android.R.style.Theme_DeviceDefault)

		setContent {
			WearApp("GalaxyTime") { navigateToWatchFace() }
		}
	}

	fun navigateToWatchFace() {
		val wf = ComponentName(this, WatchFaceCanvasRenderer::class.java)
		val intent: Intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
			.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, wf)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		this.startActivity(intent)
		finish()
	}

	companion object {
        const val TAG = "MainActivity"
	}

}
@Composable
fun WearApp(greetingName: String, action: () -> Unit) {
	GalaxyTimeTheme {
		Box(
			modifier = Modifier
				.padding(32.dp)
				.wrapContentSize()
				.background(Color.Black),
			contentAlignment = Alignment.Center

		) {
			Column(
				Modifier
					.fillMaxWidth()
					.absolutePadding(10.dp)
					.verticalScroll(rememberScrollState())
//				.weight(weight = 1f, fill = false)
			) {
				Greeting(greetingName = greetingName)
				SimpleButton(action)
			}
		}
	}
}

@Composable
fun Greeting(greetingName: String) {
	Text(
		textAlign = TextAlign.Center,
		color = Color(0xffffffff),
		fontSize = 10.sp,
		text = stringResource(R.string.hello_world, greetingName),
		modifier = Modifier
			.fillMaxWidth()
			.padding(10.dp)
	)
}

@Composable
fun SimpleButton(action: () -> Unit) {
	OutlinedButton(
		modifier = Modifier.fillMaxWidth(),
		onClick = { action() }
	) {
		Text(
			fontSize = 10.sp,
			text = "Activate"
		)
	}
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
	WearApp("GalaxyTime Preview", action = { })
}
