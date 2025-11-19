package com.example.chatappserver.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatappserver.data.websocket.MyWebsocketServerManager

/**
 * ホーム画面
 * @param viewModel : 共有するViewModel
 * @param onStop    : 画面遷移用コールバック
 */
@Composable
fun ServerHomeScreen(
    viewModel: MyWebsocketServerManager,
    onStop: () -> Unit
) {
    // ViewModelが保持している接続中ユーザーリストの監視
    val connectionUserList by viewModel.connectionUserList.collectAsState()

    // ステートレスUIコンポーネントにViewModelデータを渡すための準備
    val uiState = ServerHomeScreenUIState(
        connectionUserList = connectionUserList
    )

    // ステートレスUIコンポーネント
    ServerHomeScreenContent(
        uiState = uiState,
        onStop = onStop
    )
}