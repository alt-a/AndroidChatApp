package com.example.chatappclient.ui.screen.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatappclient.data.websocket.MyWebsocketClient
import com.example.chatappclient.data.websocket.MyWebsocketClientStatus

/**
 * チャット画面
 * @param viewModel     : 共有する ViewModel
 * @param onDisconnect  : 切断時に呼び出されるコールバック (画面遷移用)
 */
@Composable
fun ClientChatScreen(
    viewModel: MyWebsocketClient,
    onDisconnect: () -> Unit // ★コールバックを受け取る
) {
    // ViewModelが保持しているメッセージリストを監視
    // (messages.value が更新されると、 'messages' も自動的に更新 = 再描画)
    val messages by viewModel.messages.collectAsState()

    // ViewModelが保持している自分の名前
    val myName by viewModel.userName.collectAsState()

    // ViewModelが保持している接続状態の監視
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    // ステートレスUIコンポーネントにViewModelデータを渡すための準備
    val uiState = ClientChatScreenUIState(
        messages = messages,
        userName = myName,
        connectionStatus = connectionStatus
    )

    // --- 接続が切れたら自動で戻る ---
    LaunchedEffect(connectionStatus) {
        if (connectionStatus != MyWebsocketClientStatus.CONNECTED && connectionStatus != MyWebsocketClientStatus.CONNECTING) {
            // 接続状態が "Connected" 以外 (Error, Disconnected など) になったら
            // 起動時画面に戻る
            onDisconnect()
        }
    }

    // ステートレスUIコンポーネント
    ClientChatScreenContent(
        uiState = uiState,
        onDisconnect = viewModel::disconnect,
        onDisconnectButtonClick = onDisconnect,
        onRequest = viewModel::sendRequestConnectionUserInfo,
        onSendMessageButtonClick = viewModel::sendMessage
    )
}