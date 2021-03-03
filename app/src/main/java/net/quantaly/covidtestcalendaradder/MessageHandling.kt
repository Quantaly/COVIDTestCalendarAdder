package net.quantaly.covidtestcalendaradder

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.joestelmach.natty.Parser
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.EntityUtils
import java.util.*

fun handleMessage(context: Context, messageBody: String) {
    val parser = Parser()
    val groups = parser.parse(messageBody)
    for (group in groups) {
        val dates = group.dates
        for (date in dates) {
            processDate(context, messageBody, date)
            if (context.getSharedPreferences(
                    context.getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE
                ).getBoolean(context.getString(R.string.preference_enter_sweepstakes), false)
            ) {
                enterSweepstakes(context, messageBody, date)
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

    val post = HttpPost("https://www.mines.edu/coronavirus/get-tested-win-big/").also {
        val cal = Calendar.getInstance().also { it.time = date }
        it.entity = MultipartEntityBuilder.create()
            .addTextBody(
                "input_2.3",
                pref.getString(context.getString(R.string.preference_first_name), "")
            )
            .addTextBody(
                "input_2.6",
                pref.getString(context.getString(R.string.preference_last_name), "")
            )
            .addTextBody(
                "input_3",
                pref.getString(context.getString(R.string.preference_email), "")
            )
            .addTextBody(
                "input_4",
                pref.getString(context.getString(R.string.preference_phone_number), "")
            )
            .addTextBody(
                "input_5",
                accessCode
            )
            .addTextBody(
                "input_6",
                "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.YEAR)}"
            )
            .addTextBody(
                "is_submit_8",
                "1"
            )
            .addTextBody(
                "gform_submit",
                "8"
            )
            .addTextBody(
                "gform_unique_id",
                ""
            )
            .addTextBody(
                "state_8",
                // from what I can tell, this does not ever change, which is weird but welcome
                "WyJbXSIsImJiOTNmZDQ2MjM3ZjVjNzgxZjNhZmYwZDNhNmJiZGZlIl0="
            )
            .addTextBody(
                "gform_target_page_number_8",
                "0"
            )
            .addTextBody(
                "gform_source_page_number_8",
                "1"
            )
            .addTextBody(
                "gform_field_values",
                ""
            ).build()
        // can't hurt
        it.addHeader("User-Agent", "Mozilla/5.0")
    }

    HttpClients.createDefault().use { client ->
        client.execute(post).use { response ->
            if (response.code == 200) {
                showToast(context, "Entered sweepstakes")
            } else {
                showToast(context, "Failed to enter sweepstakes")
            }
            EntityUtils.consumeQuietly(response.entity)
        }
    }
}
