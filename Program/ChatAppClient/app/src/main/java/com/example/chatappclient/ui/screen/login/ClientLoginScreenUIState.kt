package com.example.chatappclient.ui.screen.login

import com.example.chatappclient.data.websocket.MyWebsocketClientStatus

/**
 * ユーザー名入力画面 ViewModel保持データ参照用データクラス
 */
data class ClientLoginScreenUIState(
    val connectionStatus: MyWebsocketClientStatus = MyWebsocketClientStatus.DISCONNECTED
)