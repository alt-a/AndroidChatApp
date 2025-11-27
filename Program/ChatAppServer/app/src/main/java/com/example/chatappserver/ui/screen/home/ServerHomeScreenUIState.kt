package com.example.chatappserver.ui.screen.home

import com.example.chatappserver.data.model.ConnectionUser

/**
 * ホーム画面 ViewModel保持データ参照用データクラス
 */
data class ServerHomeScreenUIState(
    val connectionUserList: List<ConnectionUser> = listOf(
        // プレビュー用
        ConnectionUser(1, "A"),
        ConnectionUser(2, "B"),
        ConnectionUser(3, "C")
    ),
    val ipAddress: String = "000.000.000.000"
)
