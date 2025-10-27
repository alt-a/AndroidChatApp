package com.example.wsclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // 1. Ktor HttpClientのインスタンスを作成
    // (サーバー側の `embeddedServer(Netty, ...)` と似ていますね)
    private val client = HttpClient(OkHttp) {
        install(WebSockets) {
            // WebSocket関連の設定（タイムアウトなど）があればここに追加
        }
    }

    // 2. UIに公開するための「状態」
    // 接続ステータス (例: "未接続", "接続中...", "接続済み")
    private val _connectionStatus = MutableStateFlow("未接続")// アンダースコアはViewModel内部だけで使うという目印
    val connectionStatus = _connectionStatus.asStateFlow() // asStateFlowで読み取り専用にして_を取っ払う

    // サーバーとのメッセージ履歴
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages = _messages.asStateFlow()

    // 3. WebSocketセッション(通信回線)を保持するための変数
    private var webSocketSession: DefaultClientWebSocketSession? = null

    // 4. 接続ボタンが押されたときに呼ぶ関数
    fun connect(ipAddress: String) {
        // すでに接続済みの場合は何もしない
        if (webSocketSession != null) return

        _connectionStatus.value = "接続中..."

        // ViewModelのコルーチンスコープで接続処理を行う
        viewModelScope.launch {
            try {
                // Ktor 2.x の接続方法: client.webSocketSession(...) ではなく、
                // client.webSocket(...) { ... } というブロックを使います。
                client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = ipAddress,
                    port = 8000,
                    path = "/"
                ) {
                    // この { ... } ブロックの中が「接続中」の状態です
                    // セッションは "this" として扱えます
                    webSocketSession = this
                    _connectionStatus.value = "接続済み"

                    // --- 接続成功後の処理 ---
                    // サーバーからのメッセージを継続的に監視（受信）
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val receivedText = frame.readText()
                            _messages.value = _messages.value + receivedText
                        }
                    }
                    // ---ここまで---
                }

                // WebSocket接続が (finallyの前に) 正常/異常問わず切断されると
                // この webSocket ブロックを抜けます

            } catch (e: Exception) {
                // 接続失敗
                e.printStackTrace()
                _connectionStatus.value = "接続エラー: ${e.message}"
                webSocketSession = null // セッションをリセット
            } finally {
                // (もし切断されたら)
                _connectionStatus.value = "切断されました"
                webSocketSession = null
            }
        }
    }

    // 5. 送信ボタンが押されたときに呼ぶ関数
    fun sendMessage(text: String) {
        // ViewModelのコルーチンスコープで送信
        viewModelScope.launch {
            try {
                // セッションが確立していればメッセージを送信
                webSocketSession?.send(Frame.Text(text))
                // 自分が送ったメッセージも履歴に追加
                _messages.value = _messages.value + "自分: $text"
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value = _messages.value + "送信エラー: ${e.message}"
            }
        }
    }

    // 6. 切断ボタンが押されたときに呼ぶ関数
    fun disconnect() {
        viewModelScope.launch {
            webSocketSession?.close() // セッションを閉じる
            webSocketSession = null // 変数をリセット
            _connectionStatus.value = "未接続"
            _messages.value = emptyList() // メッセージ履歴をクリア
        }
    }

    // 7. ViewModelが破棄されるときに呼ばれる
    //    ここでHttpClientを閉じてリソースを解放する (非常に重要！)
    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}