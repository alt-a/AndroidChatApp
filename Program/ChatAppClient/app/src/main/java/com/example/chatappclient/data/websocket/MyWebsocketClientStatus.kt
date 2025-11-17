package com.example.chatappclient.data.websocket

/**
 * WebSocketクライアント処理 状態管理用
 */
enum class MyWebsocketClientStatus(val text: String) {
    DISCONNECTED("Disconnected"),   // 未接続
    CONNECTING("Connecting..."),    // 接続中
    CONNECTED("Connected"),         // 接続
    ERROR("Error"),                 // エラー発生
    DISCONNECTED_ERROR("Disconnected (Error)"), // エラー終了
    SEND_ERROR("Send Error")        // 送信エラー発生
}