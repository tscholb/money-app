package com.moneyapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.moneyapp.MoneyApplication
import com.moneyapp.parser.ParserRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        val grouped = messages.groupBy { it.originatingAddress }
        val pendingResult = goAsync()
        val app = context.applicationContext as MoneyApplication

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for ((_, parts) in grouped) {
                    val full = parts.joinToString("") { it.messageBody ?: "" }
                    val timestamp = parts.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()
                    val parsed = ParserRegistry.parse(full, timestamp, source = "SMS") ?: continue
                    app.ingestor.ingest(parsed)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
