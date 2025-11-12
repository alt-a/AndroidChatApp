package com.example.chatappclient.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatappclient.ChatViewModel

/**
 * ユーザー名入力画面
 * @param viewModel 共有する ChatViewModel
 * @param onConnect 接続ボタンが押されたときのコールバック (画面遷移用)
 */
@Composable
fun ClientLoginScreen(
    viewModel: ChatViewModel,
    onConnect: () -> Unit // "() -> Unit" は「引数なし、戻り値なしの関数」という意味
) {
    // ★ViewModelの接続状態を監視
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    // ステートレスUIコンポーネントにViewModelデータを渡すための準備
    val uiState = ClientLoginScreenUIState(
        connectionStatus = connectionStatus
    )

    // ★接続状態(connectionStatus) が変化したら実行
    LaunchedEffect(connectionStatus) {
        if (connectionStatus == "Connected") {
            // 接続成功時のみ、画面遷移コールバックを呼ぶ
            onConnect()
        }
        // TODO: if (connectionStatus.startsWith("Error")) { ... }
        // (ここでエラーメッセージをトーストなどで表示すると親切)
    }

    // ステートレスUIコンポーネント
    ClientLoginScreenContent(
        uiState = uiState,
        onConnect = viewModel::connect
    )
}

/**
 * ユーザー名入力画面 ViewModel保持データ参照用データクラス
 */
data class ClientLoginScreenUIState(
    val connectionStatus: String = "Disconnected"
)

/**
 * ユーザー名入力画面 ステートレスUIコンポーネント
 * @param uiState       : ViewModelが保持するUIデータ
 * @param onConnect     : サーバー接続関数
 */
@Composable
fun ClientLoginScreenContent(
    uiState: ClientLoginScreenUIState,
    onConnect: (ip: String, name: String) -> Unit
) {
    // 画面内で使用する一時的な状態変数
    var ip by remember { mutableStateOf("192.168.11.16") } // IP入力用
    var name by remember { mutableStateOf("alta") }          // 名前入力用

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(text = "サーバーに接続", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.Companion.height(16.dp))

        // IPアドレス入力欄
        OutlinedTextField(
            value = ip,
            onValueChange = { ip = it },
            label = { Text("サーバーIPアドレス") }
        )
        Spacer(modifier = Modifier.Companion.height(8.dp))

        // 名前入力欄
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("あなたの名前") }
        )
        Spacer(modifier = Modifier.Companion.height(16.dp))

        // 接続ボタン
        Button(
            onClick = {
                // ボタンが押されたら...
                // 1. ViewModelのconnectメソッドを呼ぶ（だけにする）
//                viewModel.connect(ip, name)
                onConnect(ip, name)
            },
            // ★接続中はボタンを押せなくする
            enabled = (ip.isNotBlank() && name.isNotBlank() && uiState.connectionStatus != "Connecting...")
        ) {
            // ★接続状態に応じてボタンの文字を変える
            Text(text = if (uiState.connectionStatus == "Connecting...") "接続中..." else "接続")
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