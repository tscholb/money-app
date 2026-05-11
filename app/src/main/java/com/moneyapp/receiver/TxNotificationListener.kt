package com.moneyapp.receiver

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.moneyapp.MoneyApplication
import com.moneyapp.parser.ParserRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TxNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val packageName = sbn.packageName
        if (packageName !in WATCH_PACKAGES) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val combined = listOf(title, text, big).filter { it.isNotBlank() }.joinToString("\n")
        if (combined.isBlank()) return

        val app = applicationContext as MoneyApplication
        scope.launch {
            val parsed = ParserRegistry.parse(combined, sbn.postTime, source = "NOTIFICATION-$packageName")
                ?: return@launch
            app.ingestor.ingest(parsed)
        }
    }

    companion object {
        private val WATCH_PACKAGES = setOf(
            "com.kbcard.cxh.appcard",
            "com.samsungcard.mpocket",
            "com.kakaobank.channel",
            "viva.republica.toss",
            "com.kakao.talk",
            "com.kebhana.hanapush"
        )
    }
}
