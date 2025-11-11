package com.example.chatappclient.ui.screen.chat

import com.example.chatappclient.ChatMessage

/**
 * チャット画面 ViewModel保持データ表示用データクラス
 */
data class ClientChatScreenUIState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            user = "ユーザー1",
            message = "こんにちは。"
        ),
        ChatMessage(
            user = "ユーザー2",
            message = "おつかれさまです。"
        )
    ),
    val userName: String = "ユーザー1",
    val connectionStatus: String = "Disconnected"
)