package com.example.chatappclient.ui.screen.chat

import com.example.chatappclient.data.model.MessageBroadcast
import com.example.chatappclient.data.model.ConnectionUser
import com.example.chatappclient.data.websocket.MyWebsocketClientStatus

/**
 * チャット画面 ViewModel保持データ表示用データクラス
 */
data class ClientChatScreenUIState(
    val messages: List<MessageBroadcast> = listOf(
        MessageBroadcast(
            user = "ユーザー1",
            message = "こんにちは。"
        ),
        MessageBroadcast(
            user = "ユーザー2",
            message = "おつかれさまです。"
        )
    ),
    val userName: String = "ユーザー1",
    val connectionStatus: MyWebsocketClientStatus = MyWebsocketClientStatus.DISCONNECTED,
    val connectionUserList: List<ConnectionUser> = listOf(
        ConnectionUser(id = 1, name = "A"),
        ConnectionUser(id = 2, name = "B"),
        ConnectionUser(id = 3, name = "C")
    )
)