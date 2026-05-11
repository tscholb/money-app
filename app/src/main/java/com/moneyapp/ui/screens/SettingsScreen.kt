package com.moneyapp.ui.screens

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneyapp.MoneyApplication
import com.moneyapp.backup.BackupManager
import com.moneyapp.receiver.TxNotificationListener
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backup = remember {
        BackupManager(context, (context.applicationContext as MoneyApplication).repository)
    }
    var status by remember { mutableStateOf<String?>(null) }

    val exportJson = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) scope.launch {
            val n = backup.exportJson(uri)
            status = "JSON ${n}건 내보내기 완료"
        }
    }

    val exportCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) scope.launch {
            val n = backup.exportCsv(uri)
            status = "CSV ${n}건 내보내기 완료"
        }
    }

    val importJson = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) scope.launch {
            val n = backup.importJson(uri)
            status = "JSON ${n}건 불러오기 완료"
        }
    }

    LaunchedEffect(status) {
        if (status != null) {
            kotlinx.coroutines.delay(2500)
            status = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("권한", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        OutlinedButton(
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("SMS 권한 설정 열기") }

        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    ComponentName(context, TxNotificationListener::class.java).flattenToString()
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("알림 접근 권한 설정 열기") }

        Spacer(Modifier.height(8.dp))
        Text("백업", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Button(
            onClick = { exportJson.launch("moneyapp-backup.json") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("JSON으로 내보내기") }

        OutlinedButton(
            onClick = { exportCsv.launch("moneyapp-backup.csv") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("CSV로 내보내기") }

        OutlinedButton(
            onClick = { importJson.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("JSON 불러오기") }

        status?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(8.dp))
        Text("정보", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "지원 카드/은행: 삼성카드, KB국민카드, 하나은행\n" +
                "수집 방식: SMS 승인문자 + 알림(NotificationListener)\n" +
                "모든 데이터는 기기에만 저장됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
