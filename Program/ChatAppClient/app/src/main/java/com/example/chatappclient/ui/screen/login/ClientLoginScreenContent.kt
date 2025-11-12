package com.example.chatappclient.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * ユーザー名入力画面 ステートレスUIコンポーネント
 * @param uiState       : ViewModelが保持するUIデータ
 * @param onConnect     : サーバー接続関数
 */
@OptIn(ExperimentalMaterial3Api::class) // ★TopAppBar用に必要
@Composable
fun ClientLoginScreenContent(
    uiState: ClientLoginScreenUIState,
    onConnect: (ip: String, name: String) -> Unit
) {
    // 画面内で使用する一時的な状態変数
    var ip by remember { mutableStateOf("192.168.11.17") } // IP入力用
    var name by remember { mutableStateOf("alta") }          // 名前入力用

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ユーザー名設定") },
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "戻る",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding(),      // ソフトキーボード表示時に押し上げる
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(text = "ユーザー名を入力してください", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.Companion.height(16.dp))

            // 名前入力欄
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("あなたの名前") }
            )
            Spacer(modifier = Modifier.Companion.height(16.dp))

            // 接続ボタン
            ElevatedButton(
                onClick = {
                    // ボタンが押されたら...
                    // 1. ViewModelのconnectメソッドを呼ぶ（だけにする）
//                viewModel.connect(ip, name)
                    onConnect(ip, name)
                },
                // ★接続中はボタンを押せなくする
                enabled = (ip.isNotBlank() && name.isNotBlank() && uiState.connectionStatus != "Connecting..."),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                // ★接続状態に応じてボタンの文字を変える
                Text(text = if (uiState.connectionStatus == "Connecting...") "接続中..." else "OK")
            }
        }
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ClientLoginScreenContentPreview() {
    ClientLoginScreenContent(
        uiState = ClientLoginScreenUIState(),
        onConnect = { ip, name ->
            // 何もしない
        }
    )
}