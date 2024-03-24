/*
 * Copyright 2024 The Galaxy Time Project
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

package jp.lab75.galaxytime.settings

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope

import jp.lab75.galaxytime.R
import jp.lab75.galaxytime.data.watchface.ColorStyleIdAndResourceIds
import jp.lab75.galaxytime.databinding.WatchfaceSettingsBinding

// import jp.lab75.galaxytime.utils.TOP_LEFT_COMPLICATION_ID
// import jp.lab75.galaxytime.utils.TOP_RIGHT_COMPLICATION_ID
// import jp.lab75.galaxytime.utils.BOTTOM_LEFT_COMPLICATION_ID
// import jp.lab75.galaxytime.utils.BOTTOM_RIGHT_COMPLICATION_

import android.widget.RadioGroup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.R.anim

class WatchFaceSettingsActivity : ComponentActivity() {

	private val stateHolder: WatchFaceSettingsState by lazy {
		WatchFaceSettingsState(
			lifecycleScope,
			this@WatchFaceSettingsActivity
		)
	}

	// xml layout binding
	private lateinit var binding: WatchfaceSettingsBinding

	override fun finish(){
		super.finish()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Log.d(TAG, "onCreate()")

		binding = WatchfaceSettingsBinding.inflate(layoutInflater)
		setContentView(binding.root)

//		binding.radioGroup.isEnabled = false

		val radioGroup = findViewById<RadioGroup>(R.id.radio_group)

		radioGroup.setOnCheckedChangeListener { _, checkedId ->
			Log.d(TAG,"update planet selection: $checkedId")
			when (checkedId) {
				R.id.id_mercury -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.MERCURY.id )
				R.id.id_venus -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.VENUS.id )
				R.id.id_earth -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.EARTH.id )
				R.id.id_moon -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.MOON.id )
				R.id.id_mars -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.MARS.id )
				R.id.id_jupiter -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.JUPITER.id )
				R.id.id_saturn -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.SATURN.id )
				R.id.id_uranus -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.URANUS.id )
				R.id.id_neptune -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.NEPTUNE.id )
				R.id.id_pluto -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.PLUTO.id )
			}
			finish()
        }

		lifecycleScope.launch(Dispatchers.Main.immediate) {
			stateHolder.uiState
				.collect { uiState: WatchFaceSettingsState.EditWatchFaceUiState ->
					when (uiState) {
						is WatchFaceSettingsState.EditWatchFaceUiState.Loading -> {
							Log.d(TAG, "StateFlow Loading: ${uiState.message}")
						}

						is WatchFaceSettingsState.EditWatchFaceUiState.Success -> {
							Log.d(TAG, "StateFlow Success.")
							updateWatchFaceEditorPreview(uiState.userStylesAndPreview)
						}

						is WatchFaceSettingsState.EditWatchFaceUiState.Error -> {
							Log.e(TAG, "Flow error: ${uiState.exception}")
						}
					}
				}
		}
	}

	private fun updateWatchFaceEditorPreview(
		userStylesAndPreview: WatchFaceSettingsState.UserStylesAndPreview
	) {
		Log.d(TAG, "updateWatchFacePreview: $userStylesAndPreview")
		val colorStyleId: String = userStylesAndPreview.colorStyleId
		Log.d(TAG, "\tselected color style: $colorStyleId")
		binding.preview.watchFaceBackground.setImageBitmap(userStylesAndPreview.previewImage)
		enableWidgets()
	}

	private fun enableWidgets() {
//		 binding.radioGroup.isEnabled = true
	}

	companion object {
		const val TAG = "WatchFaceSettingsActivity"
	}
}
