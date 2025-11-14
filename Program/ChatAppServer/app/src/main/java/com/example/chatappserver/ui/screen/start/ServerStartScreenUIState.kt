package com.example.chatappserver.ui.screen.start

import com.example.chatappserver.data.websocket.MyWebsocketServerStatus

/**
 * アプリ起動時画面 ViewModel保持データ参照用データクラス
 */
data class ServerStartScreenUIState(
    val isServerRunning: MyWebsocketServerStatus = MyWebsocketServerStatus.DISCONNECTED,
    val onStartServer: () -> Unit
)