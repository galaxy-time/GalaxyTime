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
package jp.lab75.galaxytime.data.watchface

// Defaults for the watch face. All private values aren't editable by the user, so they don't need
// to be exposed as settings defaults.

const val DRAW_HOUR_PIPS_DEFAULT = true
const val DRAW_COMPLICATIONS_DEFAULT = false
const val DRAW_BACKGROUND_IMAGE_DEFAULT = false

//private const val SECOND_DIAL_WIDTH = 10f
//private const val MINUTE_DIAL_WIDTH = 10f
//private const val HOUR_DIAL_WIDTH = 10f
//private const val DAY_DIAL_WIDTH = 0f

// for publicly accessible values, defaults are set like this:
// const val EXAMPLE_DEFAULT = 1.234f
// const val EXAMPLE_MINIMUM = 1.0f
// const val EXAMPLE_MAXIMUM = 2.0f

//private const val GAP_BETWEEN_DIALS = 1f
//private const val BEZEL_WIDTH = 20f

/**
 * Represents all data needed to render an analog watch face.
 */
data class WatchFaceData(

	val activeColorStyle: ColorStyleIdAndResourceIds = ColorStyleIdAndResourceIds.EARTH,
	val ambientColorStyle: ColorStyleIdAndResourceIds = ColorStyleIdAndResourceIds.AMBIENT,

	val drawComplications: Boolean = DRAW_COMPLICATIONS_DEFAULT,
	val drawBackgroundImage: Boolean = DRAW_BACKGROUND_IMAGE_DEFAULT,

//	val drawHourPips: Boolean = DRAW_HOUR_PIPS_DEFAULT,

//	val secondDialWidth: Float = SECOND_DIAL_WIDTH,
//	val minuteDialWidth: Float = MINUTE_DIAL_WIDTH,
//	val hourDialWidth: Float = HOUR_DIAL_WIDTH,
//	val dayDialWidth: Float = DAY_DIAL_WIDTH,
//	val gapBetweenDials: Float = GAP_BETWEEN_DIALS,
//	val bezelWidth: Float = BEZEL_WIDTH

//	 val backgroundImage: ImageResource,

    // val hourHandDimensions: ArmDimensions = ArmDimensions(
    //     lengthFraction = HOUR_HAND_LENGTH_FRACTION,
    //     widthFraction = HOUR_HAND_WIDTH_FRACTION,
    //     xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
    //     yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
    // ),
    // val minuteHandDimensions: ArmDimensions = ArmDimensions(
    //     lengthFraction = MINUTE_HAND_LENGTH_FRACTION_DEFAULT,
    //     widthFraction = MINUTE_HAND_WIDTH_FRACTION,
    //     xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
    //     yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
    // ),
    // val secondHandDimensions: ArmDimensions = ArmDimensions(
    //     lengthFraction = SECOND_HAND_LENGTH_FRACTION,
    //     widthFraction = SECOND_HAND_WIDTH_FRACTION,
    //     xRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS,
    //     yRadiusRoundedCorners = ROUNDED_RECTANGLE_CORNERS_RADIUS
    // ),
)
