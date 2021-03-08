package net.quantaly.covidtestcalendaradder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        var address = ""
        var body = ""
        for (message in messages) {
            address = message.originatingAddress ?: ""
            body += message.messageBody
        }

        if (address == context.getString(R.string.incoming_number) &&
            body.contains(context.getString(R.string.match_text))
        ) {
            handleMessage(context, body)
        }
    }
}