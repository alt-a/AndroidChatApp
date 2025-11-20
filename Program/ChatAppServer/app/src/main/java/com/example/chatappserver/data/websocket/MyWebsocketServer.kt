package com.example.chatappserver.data.websocket

import com.example.chatappserver.data.model.ConnectionUser
import com.example.chatappserver.data.model.ConnectionUserList
import com.example.chatappserver.data.model.FrameID
import com.example.chatappserver.data.model.MessageBroadcast
import com.example.chatappserver.data.model.MessageSpecified
import com.example.chatappserver.data.model.MessageToYou
import com.example.chatappserver.data.model.RequestConnectionUserInfo
import com.example.chatappserver.data.model.UserID
import com.example.chatappserver.data.model.UserName
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.Collections
import java.util.LinkedHashSet

class MyWebsocketServer {

    // 接続(セッション)を管理するスレッドセーフなSet(リスト)
    // DefaultWebSocketSession は Ktor の WebSocket 接続そのものを表します
    // LinkedHashSetは重複のない接続順を保持したリスト
    // Collections.synchronizedSetはリストの追加削除を1つずつ実行するオブジェクト
    private val _connections = MutableStateFlow(Collections.synchronizedSet(LinkedHashSet<IdentifiedSession>()))
    val connections: StateFlow<Set<IdentifiedSession>> = _connections.asStateFlow()

    // ユーザーID生成カウンター
    private var nextId: Int = 1

    // セッションリスト操作の競合防止
    private val mutex = Mutex()

    // 接続中ユーザーリスト
    private val _userList = MutableStateFlow<List<ConnectionUser>>(emptyList())
    val userList: StateFlow<List<ConnectionUser>> = _userList.asStateFlow()

    // サーバー本体 (Nettyエンジン, ポート8080)
    // ※エコーサーバーの時は8000でしたが、今回は8080にしておきます (お好みで変更OK)
    private val netty = embeddedServer(Netty, port = 8080) {

        // JSONプラグインのインストール
        install(ContentNegotiation) {
            // kotlinx.serialization を使う設定
            json(Json {
                ignoreUnknownKeys = true        // JSONに未知のキーがあっても無視
                classDiscriminator = "content"  // 識別子キー
            })
        }

        // WebSocketプラグインのインストール
        install(WebSockets.Plugin) {
            pingPeriod = Duration.ofMinutes(1) // 1分ごとに生存確認
            timeout = Duration.ofSeconds(15)   // 15秒応答がなければタイムアウト
            maxFrameSize = Long.MAX_VALUE      // フレームサイズ制限なし
            masking = false                    // マスキング無効
        }

        // ルーティング設定
        routing {
            webSocket("/") { // ルートパス ( ws://...:8080/ ) への接続

                // 1. 新しいクライアントが接続してきたら、管理リストに追加
                println("Connection established! Adding session: $this")
                val newSession = addSession(this)   // 'this' が接続してきたセッションです

                // メッセージ受信ループ (接続が切れるまでここで待ち続ける)
                listenForMessages(newSession)
            }
        }
    }

    /** サーバーを起動する */
    fun start() {
        // wait=true で、サーバーが停止するまでこのスレッドをブロックする
        netty.start(wait = true)
    }

    /**
     * メッセージ受信ループ
     * @param targetSession : 対象のWebSocketセッション
     */
    private suspend fun listenForMessages(targetSession: IdentifiedSession) {
        try {
            // 2. メッセージ受信ループ
            targetSession.session.incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val receivedText = frame.readText()
                    println("Received: $receivedText. Broadcasting to ${connections.value.size} clients...")

                    // JSON解析
                    val clientMessage: FrameID = Json.decodeFromString(receivedText)

                    // 受信フレームの内容により処理を分岐
                    when (clientMessage) {
                        // ----- ユーザーID（受信しない） -----
                        is UserID -> {}

                        // ----- ユーザー名 -----
                        is UserName -> {
                            println("Receive username!")

                            // 接続中ユーザーリスト更新
                            // 同一IDが存在していないことを確認して追加する
                            val searchUser = _userList.value.filter { it.id == targetSession.id }
                            if (searchUser.isEmpty()) {
                                val updatedList = _userList.value.toMutableList().apply {
                                    val newUser = ConnectionUser(
                                        id = targetSession.id,
                                        name = clientMessage.name
                                    )
                                    add(newUser)
                                }
                                _userList.value = updatedList
                            }

                            // クライアントにユーザーIDを通知
                            if (targetSession.session.isActive) {
                                // 送信データ作成（フレーム識別子付きJSON文字列）
                                val id = UserID(id = targetSession.id)
                                val jsonString = Json.encodeToString(FrameID.serializer(), id)

                                // ユーザーIDフレームをクライアントへ送信
                                targetSession.session.send(Frame.Text(jsonString))
                            }
                        }

                        // ----- 接続中ユーザー情報要求 -----
                        is RequestConnectionUserInfo -> {
                            println("Receive request!")

                            // 接続中ユーザー一覧を返す
                            // 送信データ作成（フレーム識別子付きJSON文字列）
                            val list = ConnectionUserList(
                                list = _userList.value
                            )
                            val jsonString = Json.encodeToString(FrameID.serializer(), list)

                            // 接続中ユーザー一覧フレームをクライアントへ送信
                            targetSession.session.send(Frame.Text(jsonString))
                        }

                        // ----- 接続中ユーザー一覧（受信しない） -----
                        is ConnectionUserList -> {}

                        // ----- ブロードキャストメッセージ -----
                        is MessageBroadcast -> {
                            // 3. 接続している全員にメッセージを中継 (ブロードキャスト)
                            connections.value.forEach { session ->
                                // 念のため、セッションがアクティブか確認
                                if (session.session.isActive) {
                                    // 送られてきたテキスト(JSON文字列)をそのまま送る
                                    session.session.send(Frame.Text(receivedText))
                                }
                            }
                        }

                        // ----- 個別メッセージ -----
                        is MessageSpecified -> {}

                        // ----- メッセージ（送信用フレーム・受信しない） -----
                        is MessageToYou -> {}
                    }
                }
            }
        } catch (e: Exception) {
            // クライアントが切断した場合 (アプリを閉じた、通信が途切れたなど)
            println("Connection error: ${e.localizedMessage}")

        } finally {
            // 4. 接続が切れたら (try-finally)、必ず管理リストから削除
            println("Connection closed. Removing session: ${targetSession.session}")
            val rmSession = removeSession(targetSession.id)

            // 接続中ユーザーリスト更新
            if (rmSession != null) {
                val currentList = _userList.value
                val removeUser = currentList.filter { it.id == rmSession.id }

                val updatedList = currentList - removeUser
                _userList.value = updatedList
            }
        }
    }

    /** サーバーを停止する */
    fun stop() {
        // 猶予期間3秒、タイムアウト10秒で優雅に停止
        netty.stop(
            gracePeriodMillis = 3000,   // 接続がクローズされるまで待機する猶予時間【最低3秒】
            timeoutMillis = 10000       // 強制終了までの総タイムアウト
        )
        clearAllSessions()  // 念のためリストもクリア
    }

    /**
     * WebSocketセッションにユーザーIDを割り当て、リストに追加
     * （mutexによりリスト操作の競合防止）
     * @param session   : 新しいWebSocketセッション
     * @return          : ユーザーIDを発行したセッション
     */
    private suspend fun addSession(session: DefaultWebSocketSession): IdentifiedSession {
        mutex.withLock {
            // セッションにユーザーIDを割り当てる
            val newIdentifiedSession: IdentifiedSession
            newIdentifiedSession = IdentifiedSession(nextId, session)

            // セッションリスト更新
            val updatedSet = _connections.value + newIdentifiedSession
            _connections.value = updatedSet

            // ユーザーIDインクリメント
            nextId++

            return newIdentifiedSession
        }
    }

    /**
     * WebSocketセッションをリストから削除
     * （mutexによりリスト操作の競合防止）
     * @param id    : 削除したいWebSocketセッションのユーザーID
     * @return      : 削除対象セッション＆ID
     */
    private suspend fun removeSession(id: Int): IdentifiedSession? {
        mutex.withLock {
            // 現在のセッションリストから削除対象を抽出
            val currentSet = _connections.value
            val sessionToRemoveData = currentSet.find { it.id == id }

            // セッションリストから削除
            if (sessionToRemoveData != null) {
                val updatedSet = currentSet - sessionToRemoveData
                _connections.value = updatedSet
            }

            return sessionToRemoveData
        }
    }

    /**
     * すべてのWebSocketセッションをリストからクリア
     */
    private fun clearAllSessions() {
        _connections.value = emptySet()
        nextId = 1
    }
}