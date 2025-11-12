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