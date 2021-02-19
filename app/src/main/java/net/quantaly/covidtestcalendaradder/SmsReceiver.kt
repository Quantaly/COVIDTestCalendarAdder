package net.quantaly.covidtestcalendaradder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (message in messages) {
            if (message.originatingAddress == context.getString(R.string.incoming_number) &&
                message.messageBody.contains(context.getString(R.string.match_text))
            ) {
                handleMessage(context, message.messageBody)
                showToast(context, context.getString(R.string.event_added_toast))
            }
        }
    }
}