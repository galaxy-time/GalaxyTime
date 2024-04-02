package jp.lab75.galaxytime.views

import android.util.Log
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import jp.lab75.galaxytime.R

class SplashScreenActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash_screen)

		val videoView = findViewById<VideoView>(R.id.videoView)
		val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.intro}")
		Log.d("video","videoUri: $videoUri")
		videoView.setVideoURI(videoUri)

		val mediaController = MediaController(this)
		videoView.setMediaController(mediaController)

		videoView.setOnPreparedListener { mediaPlayer ->
			mediaPlayer.isLooping = false
			mediaPlayer.start()
		}

		videoView.setOnCompletionListener { _ ->
			navigateToMainActivity()
		}
	}

	private fun navigateToMainActivity() {
		val intent = Intent(this, MainActivity::class.java)
		startActivity(intent)
		finish()
	}
	companion object {
		private const val TAG = "Splash"
	}
}
