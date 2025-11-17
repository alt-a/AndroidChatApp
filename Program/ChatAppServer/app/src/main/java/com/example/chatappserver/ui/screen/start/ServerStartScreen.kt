package com.example.chatappserver.ui.screen.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatappserver.data.websocket.MyWebsocketServerManager
import com.example.chatappserver.data.websocket.MyWebsocketServerStatus

/**
 * アプリ起動時画面
 * @param viewModel : 共有するViewModel
 * @param onStartup : 「はじめる」ボタン押下時 画面遷移用コールバック
 */
@Composable
fun ServerStartScreen(
    viewModel: MyWebsocketServerManager,
    onStartup: () -> Unit
) {
    // サーバー起動状態を監視
    val isServerRunning by viewModel.isServerRunning.collectAsState()

    // ステートレスUIコンポーネントにViewModelデータを渡すための準備
    val uiState = ServerStartScreenUIState(
        isServerRunning = isServerRunning,
        onStartServer = viewModel::startServer
    )

    LaunchedEffect(isServerRunning) {
        if (isServerRunning == MyWebsocketServerStatus.CONNECTED) {
            // サーバー起動時、ホーム画面へ遷移
            onStartup()
        }
    }

    // ステートレスUIコンポーネント
    ServerStartScreenContent(
        uiState = uiState
    )
}