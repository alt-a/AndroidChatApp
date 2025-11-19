package com.example.chatappserver.data.websocket

import io.ktor.websocket.DefaultWebSocketSession

/**
 * WebSocketセッション ユーザーIDと紐づけて管理するデータクラス
 * @property id         : ユーザーID
 * @property session    : WebSocketセッション
 */
data class IdentifiedSession(
    val id: Int,
    val session: DefaultWebSocketSession
)