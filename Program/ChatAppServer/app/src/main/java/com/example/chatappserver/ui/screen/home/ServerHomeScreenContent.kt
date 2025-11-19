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

/**
 * ホーム画面 ステートレスUIコンポーネント
 * @param uiState   : ViewModelが保持するUIデータ
 * @param onStop    : 画面遷移用コールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerHomeScreenContent(
    uiState: ServerHomeScreenUIState,
    onStop: () -> Unit
) {
    // リストスクロール状態管理
    val scrollState = rememberScrollState()

    // アラート表示状態管理
    val showAlert = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {  // 戻るボタン
                    IconButton(
                        onClick = {
                            // サーバー終了確認アラート表示
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
                title = { Text(text = "接続中ユーザー一覧") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues) // Scaffoldからのpaddingを適用
                .padding(all = 8.dp)    // コンテンツ自体のpadding
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            // ユーザー情報表示
            uiState.connectionUserList.forEach { user ->
                ConnectionUserCard(
                    id = user.id.toString(),
                    name = user.name
                )
            }
        }
    }

    // ----- サーバー終了確認アラート表示 -----
    if (showAlert.value) {
        StopServerConfirmAlert(
            onDismissRequest = { showAlert.value = false }, // "戻る"
            onConfirm = {   // "OK"
                showAlert.value = false
                onStop()    // 起動時画面に戻る
            }
        )
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ServerHomeScreenContentPreview() {
    ServerHomeScreenContent(
        uiState = ServerHomeScreenUIState(),
        onStop = {}
    )
}