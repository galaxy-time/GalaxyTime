package jp.lab75.galaxytime

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

		Log.d(TAG, "main()")

		installSplashScreen()
		super.onCreate(savedInstanceState)

		// setContent {
			//			WearApp("Wear OS app")
		// }

	}

	companion object {
        private const val TAG = "MainActivity"
    }
}
