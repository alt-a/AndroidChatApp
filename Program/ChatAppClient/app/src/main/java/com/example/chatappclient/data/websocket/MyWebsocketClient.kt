package com.example.chatappclient.data.websocket

import com.example.chatappclient.data.model.MessageBroadcast
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappclient.data.model.ConnectionUser
import com.example.chatappclient.data.model.ConnectionUserList
import com.example.chatappclient.data.model.FrameID
import com.example.chatappclient.data.model.MessageSpecified
import com.example.chatappclient.data.model.MessageToYou
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant

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
    private val _messages = MutableStateFlow<List<MessageBroadcast>>(emptyList())
    val messages = _messages.asStateFlow()

    // 自分のユーザーID
    private var myID: Int = 0

    // 接続中ユーザー一覧 状態管理
    private val _userList = MutableStateFlow<List<ConnectionUser>>(emptyList())
    val userList: StateFlow<List<ConnectionUser>> = _userList.asStateFlow()

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
                            myID = serverMessage.id
                        }

                        // ----- ユーザー名（受信しない） -----
                        is UserName -> {}

                        // ----- 接続中ユーザー情報要求（受信しない） -----
                        is RequestConnectionUserInfo -> {}

                        // ----- 接続中ユーザー一覧 -----
                        is ConnectionUserList -> {
                            Log.d("MyWebsocketClient", "Receive UserList: ${serverMessage.list}")

                            // リスト更新
                            // 念のため自分がリストに入っていることを確認し、リストから除去
                            val me = serverMessage.list.find { it.id == myID }
                            if (me != null) {
                                val updateList = serverMessage.list - me
                                _userList.value = updateList
                            }
                        }

                        // ----- ブロードキャストメッセージ -----
                        is MessageBroadcast -> {
                            // メッセージリストの「末尾」に新しいメッセージを追加
                            // (UIが更新される)
                            _messages.value = _messages.value + serverMessage
                        }

                        // ----- 個別メッセージ（送信用フレーム・受信しない） -----
                        is MessageSpecified -> {}

                        // ----- メッセージ -----
                        is MessageToYou -> {
                            Log.d("MyWebsocketClient", "Receive message!: $serverMessage")
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
     * サーバーに接続中ユーザー情報を要求する
     */
    fun sendRequestConnectionUserInfo() {
        // 未接続なら何もしない
        if (webSocketSession == null || !webSocketSession!!.isActive) return

        Log.d("MyWebsocketClient", "Attempt to send request.")

        // 送信データ作成（フレーム識別子付きJSON文字列）
        val request = RequestConnectionUserInfo
        val jsonString = Json.encodeToString(FrameID.serializer(), request)

        viewModelScope.launch {
            try {
                // 接続中ユーザー情報要求フレームをサーバーへ送信
                Log.d("MyWebsocketClient", "Submit request.")
                webSocketSession?.send(Frame.Text(jsonString))

            } catch (e: Exception) {
                Log.e("MyWebsocketClient", "Error in sendRequestConnectionUserInfo!", e)

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
        val myMessage = MessageBroadcast(
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
     * 個別メッセージを送信する
     * @param to            : 送信先ユーザーIDリスト
     * @param messageText   : メッセージ本文
     */
    fun sendMessageSpecified(to: List<Int>, messageText: String) {
        // 未接続、またはメッセージが空なら何もしない
        if (webSocketSession == null || !webSocketSession!!.isActive || messageText.isBlank()) return

        // 送信先リストに自分を追加
        // 念のため自分がリストに入っていないことを確認し、リストに追加
        var toList = to
        val me = to.find { it == myID }
        if (me == null) {
            val updateList = to + myID
            toList = updateList
        }

        // 現在時刻を取得（Unix時間）
        val currentUnixTime = Instant.now().epochSecond

        // 送信データ作成（フレーム識別子付きJSON文字列）
        val myMessage = MessageSpecified(
            to = toList,
            from = myID,
            message = messageText,
            timestamp = currentUnixTime
        )
        val jsonString = Json.encodeToString(FrameID.serializer(), myMessage)

        viewModelScope.launch {
            try {
                // 個別メッセージフレームをサーバーへ送信
                Log.d("MyWebsocketClient", "Submit specified message.")
                webSocketSession?.send(Frame.Text(jsonString))

            } catch (e: Exception) {
                Log.e("MyWebsocketClient", "Error in sendMessageSpecified!", e)

                // 送信失敗
                _connectionStatus.value = MyWebsocketClientStatus.SEND_ERROR
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