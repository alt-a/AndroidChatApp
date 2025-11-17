package com.example.chatappclient.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chatappclient.data.websocket.MyWebsocketClient

/**
 * ユーザー名入力画面
 * @param viewModel : 共有する ViewModel
 * @param onConnect : 接続ボタンが押されたときのコールバック (画面遷移用)
 * @param onBack    : 戻るボタン押下時 画面遷移用コールバック
 */
@Composable
fun ClientLoginScreen(
    viewModel: MyWebsocketClient,
    onConnect: () -> Unit,  // "() -> Unit" は「引数なし、戻り値なしの関数」という意味
    onBack: () -> Unit
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
        onConnect = viewModel::connect,
        onBack = onBack
    )
}