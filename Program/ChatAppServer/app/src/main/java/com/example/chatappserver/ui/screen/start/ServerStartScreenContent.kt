package com.example.chatappserver.ui.screen.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatappserver.data.websocket.MyWebsocketServerStatus

/**
 * アプリ起動時画面 ステートレスUIコンポーネント
 * @param uiState   : ViewModelが保持するUIデータ
 */
@Composable
fun ServerStartScreenContent(
    uiState: ServerStartScreenUIState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
        ) {
            Text(
                text = "チャットアプリ",
                fontSize = 36.sp,
                modifier = Modifier.Companion.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "サーバー",
                fontSize = 36.sp,
                modifier = Modifier.Companion.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.Companion.height(48.dp))
            ElevatedButton(
                onClick = {
                    // 「はじめる」ボタン押下時 サーバー起動
                    if (uiState.isServerRunning == MyWebsocketServerStatus.DISCONNECTED) {
                        uiState.onStartServer()
                    }
                },
                modifier = Modifier.Companion.padding(),
                shape = RoundedCornerShape(25),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 6.dp
                ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // "停止中"のみボタン有効化
                // 起動処理を複数回行うこと・停止処理中に再度起動処理に入ってしまうことを防ぐ
                enabled = (uiState.isServerRunning == MyWebsocketServerStatus.DISCONNECTED)
            ) {
                Text(
                    text = "はじめる",
                    fontSize = 22.sp,
                    modifier = Modifier.Companion.padding(
                        vertical = 8.dp,
                        horizontal = 25.dp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ServerStartScreenContentPreview() {
    ServerStartScreenContent(
        uiState = ServerStartScreenUIState(
            onStartServer = {}
        )
    )
}