package jp.lab75.galaxytime.service
import android.util.Log
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import java.util.Calendar

class MeetingService private constructor(private val context: Context) {

	companion object {
		@Volatile private var INSTANCE: MeetingService? = null // Volatile modifier is necessary
		fun getInstance(context: Context) =
			INSTANCE ?: synchronized(this) { // synchronized to avoid concurrency problem
				INSTANCE ?: MeetingService(context).also { INSTANCE = it }
			}
		private const val TAG = "MeetingService"
	}

	init {
		Log.d(TAG,"init()")
	}

	private var todayMeetings = mutableListOf<Meeting>()

	data class Meeting(
		val id: Long,
		val title: String,
		val startTime: Long,
		val endTime: Long
	)

	// Update the cache with today's meetings
	// This method will be called periodically to keep the cache up to date maybe every 15 minutes?
	// TODO: @2075 Check timings for the method's for battery and performance
	fun update() {
		todayMeetings.clear() // Clear the current cache

		val cursor = fetchEventsForToday()
		cursor?.use {
			val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
			val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
			val dtStartIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
			val dtEndIndex = it.getColumnIndex(CalendarContract.Events.DTEND)

			while (it.moveToNext()) {
				val id = it.getLong(idIndex)
				val title = it.getString(titleIndex)
				val startTime = it.getLong(dtStartIndex)
				val endTime = it.getLong(dtEndIndex)

				todayMeetings.add(Meeting(id, title, startTime, endTime))
			}
		}
	}

	private fun fetchEventsForToday(): Cursor? {
		val calendar = Calendar.getInstance()
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.set(Calendar.SECOND, 0)
		calendar.set(Calendar.MILLISECOND, 0)

		val startOfDay = calendar.timeInMillis
		calendar.add(Calendar.DAY_OF_MONTH, 1)
		val startOfNextDay = calendar.timeInMillis

		val projection = arrayOf(
			CalendarContract.Events._ID,
			CalendarContract.Events.TITLE,
			CalendarContract.Events.DTSTART,
			CalendarContract.Events.DTEND
		)

		val selection =
			"${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"

		val selectionArgs = arrayOf(
			startOfDay.toString(),
			startOfNextDay.toString()
		)

		val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

		return context.contentResolver.query(
			CalendarContract.Events.CONTENT_URI,
			projection,
			selection,
			selectionArgs,
			sortOrder
		)
	}

	// Method to get the next upcoming meeting
	fun getNextMeeting(): Meeting? {
		val currentTime = System.currentTimeMillis()
		// Filter meetings to find the next one that starts after the current time
		return todayMeetings.firstOrNull { it.startTime >= currentTime }
	}


}
