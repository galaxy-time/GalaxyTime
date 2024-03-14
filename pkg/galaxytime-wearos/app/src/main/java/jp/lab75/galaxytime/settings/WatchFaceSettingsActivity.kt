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

	private lateinit var binding: WatchfaceSettingsBinding

	override fun finish(){
		super.finish()
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Log.d(TAG, "onCreate()")

		// inflate xml layout
		binding = WatchfaceSettingsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Disable widgets until data loads and values are set.
		// binding.colorStylePickerButton.isEnabled = false
		// binding.radioGroup = false
		// binding.randomPlanetPickerButton.isEnabled = false

		val radioGroup = findViewById<RadioGroup>(R.id.radio_group)

		radioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
//                R.id.id_sun -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.SUN.id )
				R.id.id_mercury -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.MERCURY.id )
				R.id.id_venus -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.VENUS.id )
				R.id.id_earth -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.EARTH.id )
				R.id.id_mars -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.MARS.id )
				R.id.id_jupiter -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.JUPITER.id )
				R.id.id_saturn -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.SATURN.id )
				R.id.id_uranus -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.URANUS.id )
				R.id.id_neptune -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.NEPTUNE.id )
				R.id.id_pluto -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.PLUTO.id )
				else -> stateHolder.setColorStyle( ColorStyleIdAndResourceIds.EARTH.id )
			}
			finish()
        }

		// binding.minuteHandLengthSlider.addOnChangeListener { slider, value, fromUser ->
		//     Log.d(TAG, "addOnChangeListener(): $slider, $value, $fromUser")
		//     if (fromUser) {
		//         stateHolder.setMinuteHandArmLength(value)
		//     }
		// }

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
		// binding.colorStylePickerButton.isEnabled = true
		// binding.randomPlanetPickerButton.isEnabled = true
		// binding.radioGroup = true
	}

	// fun onClickRandomPlanetPickerButton(view: View) {
	// 	Log.d(TAG, "random() $view")
	// 	val colorStyleIdAndResourceIdsList = enumValues<ColorStyleIdAndResourceIds>()
	// 	val newColorStyle: ColorStyleIdAndResourceIds = colorStyleIdAndResourceIdsList.random()
	// 	stateHolder.setColorStyle(newColorStyle.id)
	// }

	// TODO: this needs a proper dropdown or scroll selector
	// fun onClickColorStylePickerButton(view: View) {
	// 	Log.d(TAG, "onClickColorStylePickerButton() $view")
	// 	// Selects a random color style from list.
	// 	val colorStyleIdAndResourceIdsList = enumValues<ColorStyleIdAndResourceIds>()
	// 	val newColorStyle: ColorStyleIdAndResourceIds = colorStyleIdAndResourceIdsList.random()
	// 	stateHolder.setColorStyle(newColorStyle.id)
	// }

	// fun onClickTopLeftComplicationButton(view: View) {
	//     Log.d(TAG, "onClickTopLeftComplicationButton() $view")
	//     stateHolder.setComplication(TOP_LEFT_COMPLICATION_ID)
	// }

	// fun onClickTopRightComplicationButton(view: View) {
	//     Log.d(TAG, "onClickTopRightComplicationButton() $view")
	//     stateHolder.setComplication(TOP_RIGHT_COMPLICATION_ID)
	// }

	// fun onClickBottomLeftComplicationButton(view: View) {
	//     Log.d(TAG, "onClickBottomLeftComplicationButton() $view")
	//     stateHolder.setComplication(BOTTOM_LEFT_COMPLICATION_ID)
	// }

	// fun onClickBottomRightComplicationButton(view: View) {
	//     Log.d(TAG, "onClickBottomRightComplicationButton() $view")
	//     stateHolder.setComplication(BOTTOM_RIGHT_COMPLICATION_ID)
	// }

	// fun onClickTicksEnabledSwitch(view: View) {
	//     Log.d(TAG, "onClickTicksEnabledSwitch() $view")
	//     stateHolder.setDrawPips(binding.ticksEnabledSwitch.isChecked)
	// }

	companion object {
		const val TAG = "WatchFaceSettingsActivity"
	}
}
