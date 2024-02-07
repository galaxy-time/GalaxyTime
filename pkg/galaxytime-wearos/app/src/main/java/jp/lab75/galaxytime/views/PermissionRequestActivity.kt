package jp.lab75.galaxytime.views

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionRequestActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestPermissionsIfNeeded()
	}

	private fun requestPermissionsIfNeeded() {
		val permissionsNeeded = mutableListOf<String>()

		if (ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {
			permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
		}

		if (ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.READ_CALENDAR
			) != PackageManager.PERMISSION_GRANTED
		) {
			permissionsNeeded.add(Manifest.permission.READ_CALENDAR)
		}


		if (permissionsNeeded.isNotEmpty()) {
			ActivityCompat.requestPermissions(
				this,
				permissionsNeeded.toTypedArray(),
				MY_PERMISSIONS_REQUEST
			)
		} else {
			// All permissions have been granted, proceed with the service
			finish()
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == MY_PERMISSIONS_REQUEST) {
			if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
				// All requested permissions are granted
				finish()
			} else {
				// At least one permission was denied
				showSettingsDialog()
			}
		}
	}

	private fun showSettingsDialog() {
		AlertDialog.Builder(this)
			.setMessage("Some permissions are denied. The app needs location and calendar read permissions to function properly. Please allow them in app settings.")
			.setPositiveButton("App Settings") { _, _ ->
				val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
					data = Uri.fromParts("package", packageName, null)
				}
				startActivity(intent)
				finish()
			}
			.setNegativeButton("Cancel") { dialog, _ ->
				dialog.dismiss()
				finish()
			}
			.create()
			.show()
	}

	companion object {
		const val MY_PERMISSIONS_REQUEST = 101
	}
}
