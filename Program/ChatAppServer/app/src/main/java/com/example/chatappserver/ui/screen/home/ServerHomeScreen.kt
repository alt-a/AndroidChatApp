package com.example.chatappserver.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatappserver.ui.component.ConnectionUserCard
import com.example.chatappserver.ui.component.StopServerConfirmAlert

// 画面表示用のComposable関数
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerHomeScreen(
    ipAddress: String,
    onStop: () -> Unit
) {
    val scrollState = rememberScrollState()
    val showAlert = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showAlert.value = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = { Text(text = "接続中ユーザー一覧") } // タイトル変更
            )
        }
    ) { paddingValues ->
        // ipAddressStateの値が変わると、この画面も自動で再描画される
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues) // Scaffoldからのpaddingを適用
                .padding(all = 8.dp)    // コンテンツ自体のpadding
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            ConnectionUserCard(ipAddress)
        }
    }

    if (showAlert.value) {
        StopServerConfirmAlert(
            onDismissRequest = { showAlert.value = false },
            onConfirm = {
                showAlert.value = false
                onStop()
            }
        )
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ServerHomeScreenPreview() {
    ServerHomeScreen(
        ipAddress = "000.000.000.0",
        onStop = {}
    )
}