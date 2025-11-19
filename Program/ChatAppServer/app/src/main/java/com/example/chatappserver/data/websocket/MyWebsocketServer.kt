package com.example.chatappserver.data.websocket

import com.example.chatappserver.data.model.ConnectionUser
import com.example.chatappserver.data.model.MessageBroadcast
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
        // 今は必要ないが、サーバー側でJSON読むなら要る
        install(ContentNegotiation) {
            json() // kotlinx.serialization を使う設定
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

                try {
                    // 2. メッセージ受信ループ
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val receivedText = frame.readText()
                            println("Received: $receivedText. Broadcasting to ${connections.value.size} clients...")

                            // JSON解析
                            val chatMessage = Json.decodeFromString<MessageBroadcast>(receivedText)

                            // 接続中ユーザーリスト更新
                            // リストに存在していないとき追加する
                            val searchUser = _userList.value.filter { it.name == chatMessage.user }
                            if (searchUser.isEmpty()) {
                                val updatedList = _userList.value.toMutableList().apply {
                                    val newUser = ConnectionUser(
                                        id = newSession.id,
                                        name = chatMessage.user
                                    )
                                    add(newUser)
                                }
                                _userList.value = updatedList
                            }

                            // 3. 接続している全員にメッセージを中継 (ブロードキャスト)
                            connections.value.forEach { session ->
                                // 念のため、セッションがアクティブか確認
                                if (session.session.isActive) {
                                    // 送られてきたテキスト(JSON文字列)をそのまま送る
                                    session.session.send(Frame.Text(receivedText))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // クライアントが切断した場合 (アプリを閉じた、通信が途切れたなど)
                    println("Connection error: ${e.localizedMessage}")
                } finally {
                    // 4. 接続が切れたら (try-finally)、必ず管理リストから削除
                    println("Connection closed. Removing session: $this")
                    val rmSession = removeSession(this)

                    // 接続中ユーザーリスト更新
                    if (rmSession != null) {
                        val currentList = _userList.value
                        val removeUser = currentList.filter { it.id == rmSession.id }

                        val updatedList = currentList - removeUser
                        _userList.value = updatedList
                    }
                }
            }
        }
    }

    /** サーバーを起動する */
    fun start() {
        // wait=true で、サーバーが停止するまでこのスレッドをブロックする
        netty.start(wait = true)
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
     * @param session   : 削除したいWebSocketセッション
     * @return          : 削除対象セッション＆ID
     */
    private suspend fun removeSession(session: DefaultWebSocketSession): IdentifiedSession? {
        mutex.withLock {
            // 現在のセッションリストから削除対象を抽出
            val currentSet = _connections.value
            val sessionToRemoveData = currentSet.find { it.session == session }

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