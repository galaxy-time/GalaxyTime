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

import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import jp.lab75.galaxytime.R

// Defaults for all styles.
// X_COLOR_STYLE_ID - id in watch face database for each style id.
// X_COLOR_STYLE_NAME_RESOURCE_ID - String name to display in the user settings UI for the style.
// X_COLOR_STYLE_ICON_ID - Icon to display in the user settings UI for the style.
// X_COLOR_STYLE_BG - Background image for the style. <- UNUSED

const val AMBIENT_COLOR_STYLE_ID = "ambient_style_id"
private val AMBIENT_COLOR_STYLE_NAME_RESOURCE_ID = R.string.ambient_style_name
private val AMBIENT_COLOR_STYLE_ICON_ID = R.drawable.white_style

//const val SUN_COLOR_STYLE_ID = "sun_style_id"
//private val SUN_COLOR_STYLE_NAME_RESOURCE_ID = R.string.sun_style_name
//private val SUN_COLOR_STYLE_ICON_ID = R.drawable.sun_style

const val MERCURY_COLOR_STYLE_ID = "mercury_style_id"
private val MERCURY_COLOR_STYLE_NAME_RESOURCE_ID = R.string.mercury_style_name
private val MERCURY_COLOR_STYLE_ICON_ID = R.drawable.mercury_style

const val VENUS_COLOR_STYLE_ID = "venus_style_id"
private val VENUS_COLOR_STYLE_NAME_RESOURCE_ID = R.string.venus_style_name
private val VENUS_COLOR_STYLE_ICON_ID = R.drawable.venus_style

const val EARTH_COLOR_STYLE_ID = "earth_style_id"
private val EARTH_COLOR_STYLE_NAME_RESOURCE_ID = R.string.earth_style_name
private val EARTH_COLOR_STYLE_ICON_ID = R.drawable.earth_style

const val MOON_COLOR_STYLE_ID = "moon_style_id"
private val MOON_COLOR_STYLE_NAME_RESOURCE_ID = R.string.moon_style_name
private val MOON_COLOR_STYLE_ICON_ID = R.drawable.moon_style

const val MARS_COLOR_STYLE_ID = "mars_style_id"
private val MARS_COLOR_STYLE_NAME_RESOURCE_ID = R.string.mars_style_name
private val MARS_COLOR_STYLE_ICON_ID = R.drawable.mars_style

const val JUPITER_COLOR_STYLE_ID = "jupiter_style_id"
private val JUPITER_COLOR_STYLE_NAME_RESOURCE_ID = R.string.jupiter_style_name
private val JUPITER_COLOR_STYLE_ICON_ID = R.drawable.jupiter_style

const val SATURN_COLOR_STYLE_ID = "saturn_style_id"
private val SATURN_COLOR_STYLE_NAME_RESOURCE_ID = R.string.saturn_style_name
private val SATURN_COLOR_STYLE_ICON_ID = R.drawable.saturn_style

const val URANUS_COLOR_STYLE_ID = "uranus_style_id"
private val URANUS_COLOR_STYLE_NAME_RESOURCE_ID = R.string.uranus_style_name
private val URANUS_COLOR_STYLE_ICON_ID = R.drawable.uranus_style

const val NEPTUNE_COLOR_STYLE_ID = "neptune_style_id"
private val NEPTUNE_COLOR_STYLE_NAME_RESOURCE_ID = R.string.neptune_style_name
private val NEPTUNE_COLOR_STYLE_ICON_ID = R.drawable.neptune_style

const val PLUTO_COLOR_STYLE_ID = "pluto_style_id"
private val PLUTO_COLOR_STYLE_NAME_RESOURCE_ID = R.string.pluto_style_name
private val PLUTO_COLOR_STYLE_ICON_ID = R.drawable.pluto_style

const val WHITE_COLOR_STYLE_ID = "white_style_id"
private val WHITE_COLOR_STYLE_NAME_RESOURCE_ID = R.string.white_style_name
private val WHITE_COLOR_STYLE_ICON_ID = R.drawable.white_style

/**
 * Represents watch face color style options the user can select (includes the unique id, the
 * complication style resource id, and general watch face color style resource ids).
 *
 * The companion object offers helper functions to translate a unique string id to the correct enum
 * and convert all the resource ids to their correct resources (with the Context passed in). The
 * renderer will use these resources to render the actual colors and ComplicationDrawables of the
 * watch face.
 */
enum class ColorStyleIdAndResourceIds(
    val id: String,
    @StringRes val nameResourceId: Int,
    @DrawableRes val iconResourceId: Int,
    @DrawableRes val complicationStyleDrawableId: Int,
    @ColorRes val primaryColorId: Int,
    @ColorRes val secondaryColorId: Int,
    @ColorRes val backgroundColorId: Int,
    @ColorRes val outerElementColorId: Int
) {
    AMBIENT(
        id = AMBIENT_COLOR_STYLE_ID,
        nameResourceId = AMBIENT_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = AMBIENT_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.ambient_primary_color,
        secondaryColorId = R.color.ambient_secondary_color,
        backgroundColorId = R.color.ambient_background_color,
        outerElementColorId = R.color.ambient_outer_element_color
    ),
//	SUN(
//		id = SUN_COLOR_STYLE_ID,
//        nameResourceId = SUN_COLOR_STYLE_NAME_RESOURCE_ID,
//        iconResourceId = SUN_COLOR_STYLE_ICON_ID,
//        complicationStyleDrawableId = R.drawable.complication_white_style,
//        primaryColorId = R.color.sun_primary_color,
//        secondaryColorId = R.color.sun_secondary_color,
//        backgroundColorId = R.color.sun_background_color,
//        outerElementColorId = R.color.sun_outer_element_color
//	),
	MERCURY(
		id = MERCURY_COLOR_STYLE_ID,
        nameResourceId = MERCURY_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = MERCURY_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.mercury_primary_color,
        secondaryColorId = R.color.mercury_secondary_color,
        backgroundColorId = R.color.mercury_background_color,
        outerElementColorId = R.color.mercury_outer_element_color
	),
	VENUS(
		id = VENUS_COLOR_STYLE_ID,
        nameResourceId = VENUS_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = VENUS_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.venus_primary_color,
        secondaryColorId = R.color.venus_secondary_color,
        backgroundColorId = R.color.venus_background_color,
        outerElementColorId = R.color.venus_outer_element_color
	),
	EARTH(
		id = EARTH_COLOR_STYLE_ID,
		nameResourceId = EARTH_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = EARTH_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.earth_primary_color,
        secondaryColorId = R.color.earth_secondary_color,
		backgroundColorId = R.color.earth_background_color,
        outerElementColorId = R.color.earth_outer_element_color
	),
	MOON(
		id = MOON_COLOR_STYLE_ID,
        nameResourceId = MOON_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = MOON_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.moon_primary_color,
        secondaryColorId = R.color.moon_secondary_color,
        backgroundColorId = R.color.moon_background_color,
        outerElementColorId = R.color.moon_outer_element_color
	),
	MARS(
		id = MARS_COLOR_STYLE_ID,
		nameResourceId = MARS_COLOR_STYLE_NAME_RESOURCE_ID,
		iconResourceId = MARS_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.mars_primary_color,
        secondaryColorId = R.color.mars_secondary_color,
        backgroundColorId = R.color.mars_background_color,
		outerElementColorId = R.color.mars_outer_element_color
	),
	JUPITER(
        id = JUPITER_COLOR_STYLE_ID,
        nameResourceId = JUPITER_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = JUPITER_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
		primaryColorId = R.color.jupiter_primary_color,
        secondaryColorId = R.color.jupiter_secondary_color,
        backgroundColorId = R.color.jupiter_background_color,
        outerElementColorId = R.color.jupiter_outer_element_color
	),
	SATURN(
		id = SATURN_COLOR_STYLE_ID,
        nameResourceId = SATURN_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = SATURN_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.saturn_primary_color,
        secondaryColorId = R.color.saturn_secondary_color,
        backgroundColorId = R.color.saturn_background_color,
        outerElementColorId = R.color.saturn_outer_element_color
	),
	URANUS(
		id = URANUS_COLOR_STYLE_ID,
        nameResourceId = URANUS_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = URANUS_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.uranus_primary_color,
        secondaryColorId = R.color.uranus_secondary_color,
        backgroundColorId = R.color.uranus_background_color,
        outerElementColorId = R.color.uranus_outer_element_color
	),
	NEPTUNE(
		id = NEPTUNE_COLOR_STYLE_ID,
        nameResourceId = NEPTUNE_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = NEPTUNE_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.neptune_primary_color,
        secondaryColorId = R.color.neptune_secondary_color,
        backgroundColorId = R.color.neptune_background_color,
        outerElementColorId = R.color.neptune_outer_element_color
	),
	PLUTO(
		id = PLUTO_COLOR_STYLE_ID,
        nameResourceId = PLUTO_COLOR_STYLE_NAME_RESOURCE_ID,
        iconResourceId = PLUTO_COLOR_STYLE_ICON_ID,
        complicationStyleDrawableId = R.drawable.complication_white_style,
        primaryColorId = R.color.pluto_primary_color,
        secondaryColorId = R.color.pluto_secondary_color,
		backgroundColorId = R.color.pluto_background_color,
        outerElementColorId = R.color.pluto_outer_element_color
	);
    // WHITE(
    //     id = WHITE_COLOR_STYLE_ID,
    //     nameResourceId = WHITE_COLOR_STYLE_NAME_RESOURCE_ID,
    //     iconResourceId = WHITE_COLOR_STYLE_ICON_ID,
    //     complicationStyleDrawableId = R.drawable.complication_white_style,
    //     primaryColorId = R.color.white_primary_color,
    //     secondaryColorId = R.color.white_secondary_color,
    //     backgroundColorId = R.color.white_background_color,
    //     outerElementColorId = R.color.white_outer_element_color
    // );

    companion object {
        /**
         * Translates the string id to the correct ColorStyleIdAndResourceIds object.
         */
        fun getColorStyleConfig(id: String): ColorStyleIdAndResourceIds {
			Log.d("ColorStyle","getColorStyleConfig ———> $id")
            return when (id) {
                 AMBIENT.id -> AMBIENT
                // WHITE.id -> WHITE
				// SUN.id -> SUN
				MERCURY.id -> MERCURY
				VENUS.id -> VENUS
				EARTH.id -> EARTH
				MOON.id -> MOON
				MARS.id -> MARS
				JUPITER.id -> JUPITER
				SATURN.id -> SATURN
				URANUS.id -> URANUS
				NEPTUNE.id -> NEPTUNE
				PLUTO.id -> PLUTO
				else -> EARTH
            }
        }

        /**
         * Returns a list of [UserStyleSetting.ListUserStyleSetting.ListOption] for all
         * ColorStyleIdAndResourceIds enums. The watch face settings APIs use this to set up
         * options for the user to select a style.
         */
        fun toOptionList(context: Context): List<ListUserStyleSetting.ListOption> {
            val colorStyleIdAndResourceIdsList = enumValues<ColorStyleIdAndResourceIds>()

            return colorStyleIdAndResourceIdsList.map { colorStyleIdAndResourceIds ->
                ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(colorStyleIdAndResourceIds.id),
                    context.resources,
                    colorStyleIdAndResourceIds.nameResourceId,
                    Icon.createWithResource(
                        context,
                        colorStyleIdAndResourceIds.iconResourceId
                    )
                )
            }
        }
    }
}
