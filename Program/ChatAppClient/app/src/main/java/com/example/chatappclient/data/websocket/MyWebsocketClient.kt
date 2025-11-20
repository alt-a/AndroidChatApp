package com.example.chatappclient.data.websocket

import com.example.chatappclient.data.model.ChatMessage
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappclient.data.model.ConnectionUserList
import com.example.chatappclient.data.model.FrameID
import com.example.chatappclient.data.model.RequestConnectionUserInfo
import com.example.chatappclient.data.model.UserID
import com.example.chatappclient.data.model.UserName
import io.ktor.client.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MyWebsocketClient : ViewModel() {

    // KtorのWebSocketクライアント
    private val client = HttpClient(OkHttp) {
        // JSONプラグインのインストール
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true        // JSONに未知のキーがあっても無視
                classDiscriminator = "content"  // 識別子キー
            })
        }
        // WebSocketプラグインのインストール
        install(WebSockets) {
            // WebSocket で JSON を使うためのコンバーターを明示的に指定
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    // WebSocketセッションを保持する変数
    private var webSocketSession: DefaultClientWebSocketSession? = null

    // 接続状態を管理する (UIに "接続中..." などを表示するため)
    private val _connectionStatus = MutableStateFlow(MyWebsocketClientStatus.DISCONNECTED)
    val connectionStatus = _connectionStatus.asStateFlow()

    // ユーザー名を管理する
    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    // チャットメッセージのリストを管理する
    // ★UIにはこのリストを表示します
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    /**
     * サーバーへの接続を開始する
     * @param ip 接続先のIPアドレス
     * @param name ユーザー名
     */
    fun connect(ip: String, name: String) {
        // 既に接続中なら何もしない
        if (webSocketSession != null && webSocketSession!!.isActive) return

        // ユーザー名を保存
        _userName.value = name

        // viewModelScopeで通信処理を開始 (Activityが閉じたら自動でキャンセルされる)
        viewModelScope.launch {
            try {
                // ▼▼▼ ログ追加 ① (接続試行) ▼▼▼
                Log.d("MyWebsocketClient", "Attempting to connect to ws://$ip:8080/ ...")
                // ▲▲▲ ここまで ▲▲▲
                _connectionStatus.value = MyWebsocketClientStatus.CONNECTING

                // サーバーに接続
                webSocketSession = client.webSocketSession {
                    url("ws://$ip:8080/") // サーバーのURL
                }

                // 接続成功
                _connectionStatus.value = MyWebsocketClientStatus.CONNECTED

                // ▼▼▼ ログ追加 ② (接続成功) ▼▼▼
                Log.d("MyWebsocketClient", "Success Connection!")
                // ▲▲▲ ここまで ▲▲▲

                // ★メッセージ受信ループ (接続が切れるまでここで待ち続ける)
                listenForMessages()

            } catch (e: Exception) {
                // 接続失敗
                _connectionStatus.value = MyWebsocketClientStatus.ERROR//"Error: ${e.message}"

                // ▼▼▼ ログ追加 ② (失敗原因) ▼▼▼
                Log.e("MyWebsocketClient", "Connection failed!", e)
                // ▲▲▲ ここまで ▲▲▲

                webSocketSession = null // セッションをクリア
            }
        }
    }


    /**
     * メッセージ受信ループ
     */
    private suspend fun listenForMessages() {
        try {
            webSocketSession?.incoming?.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val receivedText = frame.readText()

                    // ★デバッグログ（前回仕込んだもの）
                    Log.d("MyWebsocketClient", "Received raw: $receivedText")

                    // 受け取ったJSON文字列を FrameID オブジェクトに変換
                    val serverMessage: FrameID = Json.decodeFromString(receivedText)

                    // ★デバッグログ（前回仕込んだもの）
                    Log.d("MyWebsocketClient", "Decoded: $serverMessage")

                    // 受信フレームの内容により処理を分岐
                    when (serverMessage) {
                        // ----- ユーザーID -----
                        is UserID -> {
                            Log.d("MyWebsocketClient", "Receive UserID: ${serverMessage.id}")
                        }

                        // ----- ユーザー名（受信しない） -----
                        is UserName -> {}

                        // ----- 接続中ユーザー情報要求（受信しない） -----
                        is RequestConnectionUserInfo -> {}

                        // ----- 接続中ユーザー一覧 -----
                        is ConnectionUserList -> {}

                        // ----- ブロードキャストメッセージ -----
                        is ChatMessage -> {
                            // メッセージリストの「末尾」に新しいメッセージを追加
                            // (UIが更新される)
                            _messages.value = _messages.value + serverMessage
                        }
                    }
                }
            }

            // ----- Close ハンドシェイク -----
            // Closeフレームを受信するとincomingループを抜けここに到達する
            Log.d("MyWebsocketClient", "Close WebSocket Session")
            _connectionStatus.value = MyWebsocketClientStatus.DISCONNECTED

        } catch (e: Exception) {
            // ▼▼▼ ここにログを追加 ▼▼▼
            Log.e("MyWebsocketClient", "Error in listenForMessages!", e)
            // ▲▲▲ ここまで ▲▲▲

            // 受信中にエラー (切断など)
            _connectionStatus.value = MyWebsocketClientStatus.DISCONNECTED_ERROR
            webSocketSession = null
        }
    }

    /**
     * サーバーにユーザー名を送信する
     */
    fun sendUserName() {
        // 未接続なら何もしない
        if (webSocketSession == null || !webSocketSession!!.isActive) return

        Log.d("MyWebsocketClient", "Attempt to send username.")

        // 送信データ作成（フレーム識別子付きJSON文字列）
        val name = UserName(name = _userName.value)
        val jsonString = Json.encodeToString(FrameID.serializer(), name)

        viewModelScope.launch {
            try {
                // ユーザー名フレームをサーバーへ送信
                Log.d("MyWebsocketClient", "Submit your username.")
                webSocketSession?.send(Frame.Text(jsonString))

            } catch (e: Exception) {
                Log.e("MyWebsocketClient", "Error in sendUserName!", e)

                // 送信失敗
                _connectionStatus.value = MyWebsocketClientStatus.SEND_ERROR
            }
        }
    }

    /**
     * サーバーにメッセージを送信する
     * @param messageText 送信するメッセージ本文
     */
    fun sendMessage(messageText: String) {
        // 未接続、またはメッセージが空なら何もしない
        if (webSocketSession == null || !webSocketSession!!.isActive || messageText.isBlank()) return

        // 自分が送信するメッセージも、自分の画面に表示する
        val myMessage = ChatMessage(
            user = _userName.value, // 自分の名前
            message = messageText
        )

        // メッセージをJSON文字列に変換（フレーム識別子付き）
        val jsonString = Json.encodeToString(FrameID.serializer(), myMessage)

        // ★デバッグログ（前回仕込んだもの）
        Log.d("MyWebsocketClient", "Sending: $jsonString")

        // メッセージをサーバーに送信
        viewModelScope.launch {
            try {
                // JSON文字列送信
                webSocketSession?.send(jsonString)

            } catch (e: Exception) {
                // ▼▼▼ ここにログを追加 ▼▼▼
                Log.e("MyWebsocketClient", "Error in sendMessage!", e)
                // ▲▲▲ ここまで ▲▲▲

                // 送信失敗
                _connectionStatus.value = MyWebsocketClientStatus.SEND_ERROR//"Send Error: ${e.message}"
            }
        }
    }


    /**
     * サーバーから切断する
     */
    fun disconnect() {
        viewModelScope.launch {
            try {
                webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "User disconnected"))
            } catch (e: Exception) {
                Log.e("MyWebsocketClient", "Error during disconnect", e)
            } finally {
                webSocketSession = null
                _connectionStatus.value = MyWebsocketClientStatus.DISCONNECTED
                _messages.value = emptyList() // チャット履歴をクリア
            }
        }
    }


    /**
     * ViewModelが破棄されるときに呼ばれる (Activity終了時など)
     */
    override fun onCleared() {
        super.onCleared()
        disconnect() // 既存の切断処理を呼び出す
        client.close()
    }
}