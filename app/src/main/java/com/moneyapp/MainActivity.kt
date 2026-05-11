package com.moneyapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.moneyapp.ui.screens.AddTransactionDialog
import com.moneyapp.ui.screens.HomeScreen
import com.moneyapp.ui.screens.PendingScreen
import com.moneyapp.ui.screens.SettingsScreen
import com.moneyapp.ui.theme.MoneyAppTheme
import com.moneyapp.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory((application as MoneyApplication).repository)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* ignore */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()
        setContent {
            MoneyAppTheme { AppShell(viewModel) }
        }
    }

    private fun requestRuntimePermissions() {
        val needed = mutableListOf<String>()
        listOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS).forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                needed += it
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                needed += Manifest.permission.POST_NOTIFICATIONS
            }
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }
}

private enum class Tab(val title: String) {
    Home("홈"), Pending("대기"), Settings("설정")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(viewModel: HomeViewModel) {
    var tab by remember { mutableStateOf(Tab.Home) }
    var showAdd by remember { mutableStateOf(false) }
    val pendingCount = viewModel.pending.collectAsState().value.size

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("가계부") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.Home,
                    onClick = { tab = Tab.Home },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(Tab.Home.title) }
                )
                NavigationBarItem(
                    selected = tab == Tab.Pending,
                    onClick = { tab = Tab.Pending },
                    icon = { Icon(Icons.Filled.Inbox, contentDescription = null) },
                    label = { Text(if (pendingCount > 0) "${Tab.Pending.title} ($pendingCount)" else Tab.Pending.title) }
                )
                NavigationBarItem(
                    selected = tab == Tab.Settings,
                    onClick = { tab = Tab.Settings },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(Tab.Settings.title) }
                )
            }
        },
        floatingActionButton = {
            if (tab == Tab.Home) {
                FloatingActionButton(onClick = { showAdd = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "추가")
                }
            }
        }
    ) { padding: PaddingValues ->
        when (tab) {
            Tab.Home -> HomeScreen(viewModel, padding)
            Tab.Pending -> PendingScreen(viewModel, padding)
            Tab.Settings -> SettingsScreen(padding)
        }
        if (showAdd) {
            AddTransactionDialog(
                onDismiss = { showAdd = false },
                onSubmit = { tx ->
                    viewModel.addManual(tx)
                    showAdd = false
                }
            )
        }
    }
}
