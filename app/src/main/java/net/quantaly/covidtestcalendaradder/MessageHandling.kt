package net.quantaly.covidtestcalendaradder

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.provider.CalendarContract
import com.joestelmach.natty.Parser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import java.util.*

fun handleMessage(context: Context, messageBody: String) {
    val parser = Parser()
    val groups = parser.parse(messageBody)
    for (group in groups) {
        val dates = group.dates
        for (date in dates) {
            processDate(context, messageBody, date)
            showToast(context, context.getString(R.string.event_added_toast))
            if (context.getSharedPreferences(
                    context.getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE
                ).getBoolean(context.getString(R.string.preference_enter_sweepstakes), false)
            ) {
                GlobalScope.launch { enterSweepstakes(context, messageBody, date) }
            }
        }
    }
}

private fun processDate(context: Context, messageBody: String, date: Date) {
    val pref = context.getSharedPreferences(
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

private fun enterSweepstakes(context: Context, messageBody: String, date: Date) {
    val pref = context.getSharedPreferences(
        context.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    val accessCode = run {
        val equalsIndex = messageBody.indexOf('=')
        messageBody.substring(equalsIndex + 1)
    }

    val client = OkHttpClient()
    val cal = Calendar.getInstance().also { it.time = date }

    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "input_2.3",
            pref.getString(context.getString(R.string.preference_first_name), "")!!
        )
        .addFormDataPart(
            "input_2.6",
            pref.getString(context.getString(R.string.preference_last_name), "")!!
        )
        .addFormDataPart(
            "input_3",
            pref.getString(context.getString(R.string.preference_email), "")!!
        )
        .addFormDataPart(
            "input_4",
            pref.getString(context.getString(R.string.preference_phone_number), "")!!
        )
        .addFormDataPart(
            "input_5",
            accessCode
        )
        .addFormDataPart(
            "input_6",
            "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.YEAR)}"
        )
        .addFormDataPart(
            "is_submit_8",
            "1"
        )
        .addFormDataPart(
            "gform_submit",
            "8"
        )
        .addFormDataPart(
            "gform_unique_id",
            ""
        )
        .addFormDataPart(
            "state_8",
            // from what I can tell, this does not ever change, which is weird but welcome
            "WyJbXSIsImJiOTNmZDQ2MjM3ZjVjNzgxZjNhZmYwZDNhNmJiZGZlIl0="
        )
        .addFormDataPart(
            "gform_target_page_number_8",
            "0"
        )
        .addFormDataPart(
            "gform_source_page_number_8",
            "1"
        )
        .addFormDataPart(
            "gform_field_values",
            ""
        ).build()

    val request = Request.Builder()
        // can't hurt
        .header("User-Agent", "Mozilla/5.0")
        .url("https://www.mines.edu/coronavirus/get-tested-win-big/")
        .post(body)
        .build()

    client.newCall(request).execute().use {
        Looper.prepare()
        if (it.isSuccessful) {
            showToast(context, context.getString(R.string.entered_sweepstakes_toast))
        } else {
            showToast(context, context.getString(R.string.enter_sweepstakes_error_toast))
        }
        it.closeQuietly()
    }
}
