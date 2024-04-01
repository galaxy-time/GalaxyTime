package jp.lab75.galaxytime.service

import android.util.Log
import android.content.Context
import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BiometricsService private constructor(private val context: Context) {

	private var hydrations = mutableListOf<Long>()
	private var lastUpdate: Long = 0
	private val threshold: Long = 1000 * 60 * 60 // 1 h
	private var count: Int = 0

	private var _state = MutableStateFlow("")
	val state: StateFlow<String> = _state

	private var _dataPoints = MutableStateFlow<List<Int>>( listOf( 0,0,0,0,0,0,0,0,0,0 ) )
	val dataPoints: StateFlow<List<Int>> = _dataPoints

	fun reset() {
		Log.d(TAG,"reset()")
		hydrations.clear()
	}

	fun add() {
//		Log.d(TAG,"add()")
		val id = UUID.randomUUID().toString()
		val time = Calendar.getInstance().timeInMillis
		hydrations.add( time )
		lastUpdate = time
		count++
		updateDataPoints()
//		Log.d(TAG, "$count")
	}

	fun updateDataPoints() {
		val tmp = _dataPoints.value.toMutableList()
		tmp.add(count)
		tmp.removeAt( 0)
		_dataPoints.value = tmp.toList()
	}

	fun update() {
		val now = Calendar.getInstance().timeInMillis
		hydrations.retainAll { now - it < threshold }
		count = hydrations.count()
		updateState()
		updateDataPoints()
//		Log.d(TAG,"update() $count ${state}")
	}

	fun updateState() {
		_state.value = when( count ) {
			in 0..3 -> "CRITICAL"
			in 4..6 -> "LOW"
			in 7..11 -> "OKAY"
			else -> "HIGH"
		}
	}

	init {
		Log.d(TAG,"init()")
	}

	companion object {
		@Volatile private var INSTANCE: BiometricsService? = null
		fun getInstance(context: Context) =
			INSTANCE ?: synchronized(this) {
				INSTANCE ?: BiometricsService(context).also { INSTANCE = it }
			}
		private const val TAG = "BiometricsService"
	}

}
