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
package jp.lab75.galaxytime.utils

import android.content.Context
import android.graphics.RectF

import androidx.wear.watchface.CanvasComplicationFactory
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository

import jp.lab75.galaxytime.R

// Information needed for complications.
// All complications use the same length.
private const val COMPLICATION_LENGTH = 90f
private const val TOP_LEFT_COMPLICATION_START_ANGLE = 0f
private const val TOP_RIGHT_COMPLICATION_START_ANGLE = 90f
private const val BOTTOM_LEFT_COMPLICATION_START_ANGLE = 180f
private const val BOTTOM_RIGHT_COMPLICATION_START_ANGLE = 270f

private val DEFAULT_COMPLICATION_STYLE_DRAWABLE_ID = R.drawable.complication_white_style

// Unique IDs for each complication.
// The settings activity that supports allowing users to select their
// complication data provider requires numbers to be >= 0.
internal const val TOP_LEFT_COMPLICATION_ID = 100
internal const val TOP_RIGHT_COMPLICATION_ID = 101
internal const val BOTTOM_LEFT_COMPLICATION_ID = 102
internal const val BOTTOM_RIGHT_COMPLICATION_ID = 103

// Unique id associated with a complication and the complication types it supports ( only LONG_TEXT ).
sealed class ComplicationConfig(val id: Int, val supportedTypes: List<ComplicationType>) {
    object TopLeft : ComplicationConfig( TOP_LEFT_COMPLICATION_ID, listOf( ComplicationType.LONG_TEXT ) )
    object TopRight : ComplicationConfig( TOP_RIGHT_COMPLICATION_ID, listOf( ComplicationType.LONG_TEXT ) )
    object BottomLeft : ComplicationConfig( BOTTOM_LEFT_COMPLICATION_ID, listOf( ComplicationType.LONG_TEXT ) )
    object BottomRight : ComplicationConfig( BOTTOM_RIGHT_COMPLICATION_ID, listOf( ComplicationType.LONG_TEXT ) )
}

// Utility function that initializes default complication slots (left and right).
fun createComplicationSlotManager(
    context: Context,
    currentUserStyleRepository: CurrentUserStyleRepository,
    drawableId: Int = DEFAULT_COMPLICATION_STYLE_DRAWABLE_ID
): ComplicationSlotsManager {

	val defaultCanvasComplicationFactory =
	CanvasComplicationFactory { watchState, listener ->
		CanvasComplicationDrawable(
			ComplicationDrawable.getDrawable(context, drawableId)!!,
			watchState,
			listener
            )
        }

		val topLeftComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
			id = ComplicationConfig.TopLeft.id,
			canvasComplicationFactory = defaultCanvasComplicationFactory,
			supportedTypes = ComplicationConfig.TopLeft.supportedTypes,
			defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
				// TODO: DATA_SOURCE based on the respective segment.
				SystemDataSources.DATA_SOURCE_DAY_OF_WEEK,
				ComplicationType.LONG_TEXT
				),
		// TODO: check custom complication geometry,
		// as we need the full canvas and inside a segment
        bounds = ComplicationSlotBounds(
            RectF()
        )
    ).build()

	val topRightComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.TopRight.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.TopRight.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
			// TODO: DATA_SOURCE based on the respective segment.
            SystemDataSources.DATA_SOURCE_DAY_OF_WEEK,
            ComplicationType.LONG_TEXT
        ),
		// TODO: check custom complication geometry,
		// as we need the full canvas and inside a segment
        bounds = ComplicationSlotBounds(
            RectF()
        )
    ).build()

	val bottomLeftComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.BottomLeft.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.BottomLeft.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
			// TODO: DATA_SOURCE based on the respective segment.
            SystemDataSources.DATA_SOURCE_DAY_OF_WEEK,
            ComplicationType.LONG_TEXT
        ),
		// TODO: check custom complication geometry,
		// as we need the full canvas and inside a segment
        bounds = ComplicationSlotBounds(
            RectF()
        )
    ).build()

	val bottomRightComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.BottomRight.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.BottomRight.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
			// TODO: DATA_SOURCE based on the respective segment.
            SystemDataSources.DATA_SOURCE_DAY_OF_WEEK,
            ComplicationType.LONG_TEXT
        ),
		// TODO: check custom complication geometry,
		// as we need the full canvas and inside a segment
        bounds = ComplicationSlotBounds(
            RectF()
        )
    ).build()

    return ComplicationSlotsManager(
        listOf(topLeftComplication, topRightComplication, bottomLeftComplication, bottomRightComplication),
        currentUserStyleRepository
    )
}
