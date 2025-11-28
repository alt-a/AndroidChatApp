package com.example.chatappserver.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatappserver.data.ipaddress.IpAddressMonitorManager
import com.example.chatappserver.data.websocket.MyWebsocketServerManager

/**
 * ホーム画面
 * @param viewModel     : 共有するViewModel（WebSocket）
 * @param ipViewModel   : 共有するViewModel（IPアドレス監視）
 * @param onStop        : 画面遷移用コールバック
 */
@Composable
fun ServerHomeScreen(
    viewModel: MyWebsocketServerManager,
    ipViewModel: IpAddressMonitorManager,
    onStop: () -> Unit
) {
    // ViewModelが保持している接続中ユーザーリストの監視
    val connectionUserList by viewModel.connectionUserList.collectAsState()

    // ViewModelが保持しているIPアドレスの監視
    val ipAddress by ipViewModel.ipAddress.collectAsState()

    // ステートレスUIコンポーネントにViewModelデータを渡すための準備
    val uiState = ServerHomeScreenUIState(
        connectionUserList = connectionUserList,
        ipAddress = ipAddress
    )

    // ステートレスUIコンポーネント
    ServerHomeScreenContent(
        uiState = uiState,
        onStop = onStop
    )
}