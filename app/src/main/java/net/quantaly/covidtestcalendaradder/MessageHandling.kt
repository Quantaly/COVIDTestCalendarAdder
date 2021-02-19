package net.quantaly.covidtestcalendaradder

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.joestelmach.natty.Parser
import java.util.*

fun handleMessage(context: Context, messageBody: String) {
    val parser = Parser()
    val groups = parser.parse(messageBody)
    for (group in groups) {
        val dates = group.dates
        for (date in dates) {
            processDate(context, messageBody, date);
        }
    }
}

private fun processDate(context: Context, messageBody: String, date: Date) {
    val pref =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
    val selectedId = pref.getLong(context.getString(R.string.preference_calendar_id), -1)

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, date.time)
        put(CalendarContract.Events.DTEND, date.time + 15 * 60 * 1000)
        put(CalendarContract.Events.TITLE, context.getString(R.string.event_title))
        put(CalendarContract.Events.DESCRIPTION, messageBody)
        put(CalendarContract.Events.CALENDAR_ID, selectedId)
        // TODO detect timezone
        put(CalendarContract.Events.EVENT_TIMEZONE, "America/Denver")
    }
    val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
}

// doesn't work from the background, sadly
private fun processDateIntent(context: Context, messageBody: String, date: Date) {
    val calendarIntent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.time)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.time + 15 * 60 * 1000)
        .putExtra(CalendarContract.Events.TITLE, context.getString(R.string.event_title))
        .putExtra(CalendarContract.Events.DESCRIPTION, messageBody)
        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(calendarIntent)
}
