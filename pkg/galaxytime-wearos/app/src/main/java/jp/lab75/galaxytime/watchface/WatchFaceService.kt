/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.lab75.galaxytime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat

import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema

import jp.lab75.galaxytime.service.Calculations
import jp.lab75.galaxytime.service.MeetingService
import jp.lab75.galaxytime.utils.createComplicationSlotManager
import jp.lab75.galaxytime.utils.createUserStyleSchema
import jp.lab75.galaxytime.views.PermissionRequestActivity
import jp.lab75.galaxytime.views.CompassActivity

class WatchFaceService : WatchFaceService() {

	private val handler = Handler( Looper.getMainLooper() )
	private lateinit var calculations: Calculations
	private lateinit var meetingService: MeetingService

	private var hasPermissions = false

	val refreshLocationInterval: Long = 1000 * 60
	val refreshCalculationsInterval: Long = 1000 * 1
	val refreshMeetingServiceInterval: Long = 1000 * 60 * 5

	override fun createUserStyleSchema(): UserStyleSchema =
		createUserStyleSchema(context = applicationContext)

	override fun createComplicationSlotsManager(
		currentUserStyleRepository: CurrentUserStyleRepository
	): ComplicationSlotsManager = createComplicationSlotManager(
		context = applicationContext,
		currentUserStyleRepository = currentUserStyleRepository
	)

	private val updateLocationLoop = object : Runnable {
		override fun run() {
			// Update location every 600 seconds
			calculations.updateLocation();
			handler.postDelayed(this, refreshLocationInterval)
		}
	}

	private val updateCalculationsLoop = object : Runnable {
		override fun run() {
			// Maybe we should not update every second? its a little crazy but ok for testing
			calculations.update();
			handler.postDelayed(this, refreshCalculationsInterval)
		}
	}

	private val updateMeetingServiceLoop = object : Runnable {
		override fun run() {
			// Update location every 15 minutes
			// Check meeting
			meetingService.update();
			handler.postDelayed(this, refreshMeetingServiceInterval)
		}
	}

	override fun onCreate() {
		super.onCreate()
		calculations = Calculations(context = applicationContext)
		meetingService = MeetingService(context = applicationContext)


		if ( ContextCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED ||
			ContextCompat.checkSelfPermission(
				this, Manifest.permission.READ_CALENDAR
			) != PackageManager.PERMISSION_GRANTED
		) {

			// hasPermissions = false

			Log.d( TAG, "Insufficient permissions. Starting permission request activity." )
			val intent = Intent(this, PermissionRequestActivity::class.java)
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			startActivity(intent)

		} else {

			// hasPermissions = true

			Log.d(TAG, "Permissions granted")

			calculations.updateLocation()
			calculations.update()

			meetingService.update()

			handler.post(updateLocationLoop)
			handler.post(updateCalculationsLoop)
			handler.post(updateMeetingServiceLoop)

		}
	}

	override fun onDestroy() {
		super.onDestroy()
		handler.removeCallbacks(updateLocationLoop)
		handler.removeCallbacks(updateCalculationsLoop)
		handler.removeCallbacks(updateMeetingServiceLoop)
	}

	override suspend fun createWatchFace(
		surfaceHolder: SurfaceHolder,
		watchState: WatchState,
		complicationSlotsManager: ComplicationSlotsManager,
		currentUserStyleRepository: CurrentUserStyleRepository
	): WatchFace {
		Log.d(TAG, "createWatchFace()")

		val renderer = WatchFaceCanvasRenderer(
			context = applicationContext,
			surfaceHolder = surfaceHolder,
			watchState = watchState,
			complicationSlotsManager = complicationSlotsManager,
			currentUserStyleRepository = currentUserStyleRepository,
			canvasType = CanvasType.HARDWARE,
			calculations = calculations,
			meetingService = meetingService
		)

		return WatchFace(
			WatchFaceType.ANALOG,
			renderer
		).setTapListener(renderer)
	}

	companion object {
		const val TAG = "WatchFaceService"
	}
}
