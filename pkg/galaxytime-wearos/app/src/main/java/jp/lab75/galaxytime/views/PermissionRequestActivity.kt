package jp.lab75.galaxytime.views

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

class PermissionRequestActivity : AppCompatActivity() {
	var dialogCounter = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		ensureLocationPermission()
	}

	private fun ensureLocationPermission() {
		dialogCounter++;
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
			!= PackageManager.PERMISSION_GRANTED
		) {
			// Permission is not granted
			ActivityCompat.requestPermissions(
				this,
				arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
				MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
			)
		} else {
			// Permission has already been granted, start service and finish activity
			finish()
		}
	}

	override fun onResume() {
		super.onResume()
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
			== PackageManager.PERMISSION_GRANTED
		) {
			// Permission has already been granted, start service and finish activity
			finish()
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
				// If request is cancelled, the result arrays are empty.
				if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					// Permission was granted, start service and finish activity
					finish()
				} else {
					// Permission denied, ask again
					if (dialogCounter < 2) {
						ensureLocationPermission()
					} else {
						// Permission denied, open settings page and finish activity
						// TODO: Open settings page after the second time android will not show the dialog again so we need to disable everything that needs location data, and open the settings page if we want to use the
						// TODO: Build dialog with ok and cancel button, if ok open settings page, if cancel close and hide everything that needs location data
						AlertDialog.Builder(this)
							.setMessage("Galaxytime needs location permission to calculate the position and time of the planets.")
							.setPositiveButton("App Settings") { _, _ ->
								val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
									data = Uri.fromParts("package", packageName, null)
								}
								startActivity(intent)
								finish()
							}
							.setNegativeButton("Cancel") { dialog, _ ->
								dialog.dismiss()
								// TODO: Hide everything that needs location data
								finish()
							}
							.show()
					}

				}
				return
			}
			// Add other 'when' lines to check for other permissions this app might request.
			else -> {
				// Ignore all other requests.
			}
		}
	}


	companion object {
		// @2075 We can use any value here, but it should be unique for each permission request in this app.
		const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
	}
}
