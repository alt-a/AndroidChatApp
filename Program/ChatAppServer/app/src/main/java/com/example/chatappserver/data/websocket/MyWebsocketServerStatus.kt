package com.example.chatappserver.data.websocket

/**
 * WebSocketサーバー処理 状態管理用
 */
enum class MyWebsocketServerStatus() {
    DISCONNECTED,   // 停止中
    CONNECTED,      // 起動中
    CLOSING         // 停止処理中
}