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

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace

import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import jp.lab75.galaxytime.calculations.Calculations

import jp.lab75.galaxytime.utils.createComplicationSlotManager
import jp.lab75.galaxytime.utils.createUserStyleSchema

class WatchFaceService : WatchFaceService() {
	private val handler = Handler(Looper.getMainLooper())
	private lateinit var calculations: Calculations
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
			// Update location every 10 seconds
			calculations.updateLocation();
			handler.postDelayed(this, 1000 * 10)
		}
	}

	private val updateCalculationsLoop = object : Runnable {
		override fun run() {
			// Maybe we should not update every second? its a little crazy but ok for testing
			// Add Time mesurmants for performance information
			val startTime = System.currentTimeMillis()
			calculations.update();
			val endTime = System.currentTimeMillis()
			Log.d(TAG, "updateCalculationsLoop() ${endTime - startTime}ms")
			handler.postDelayed(this, 1000 * 1)
		}
	}

	override fun onCreate() {
		super.onCreate()

		// Add location update to main loop
		calculations = Calculations(this)
		handler.post(updateLocationLoop)
		handler.post(updateCalculationsLoop)
	}

	override fun onDestroy() {
		super.onDestroy()
		handler.removeCallbacks(updateLocationLoop)
		handler.removeCallbacks(updateCalculationsLoop)
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
			canvasType = CanvasType.HARDWARE
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
