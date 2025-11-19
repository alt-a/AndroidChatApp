package com.example.chatappserver.data.websocket

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappserver.data.model.ConnectionUser
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MyWebsocketServerManager : ViewModel() {

    // WebSocketサーバー
    private var mWebSocketServer: MyWebsocketServer? = null

    // サーバー起動状態取得用
    private val _isServerRunning = MutableStateFlow(MyWebsocketServerStatus.DISCONNECTED)
    val isServerRunning: StateFlow<MyWebsocketServerStatus> = _isServerRunning.asStateFlow()

    // 起動中のコルーチンジョブを保持するための変数
    private var serverJob: Job? = null

    // 接続中ユーザーリスト監視
    private val _connectionUserList = MutableStateFlow(listOf<ConnectionUser>())
    val connectionUserList: StateFlow<List<ConnectionUser>> = _connectionUserList.asStateFlow()

    /**
     * WebSocketサーバー起動関数
     */
    fun startServer() {
        // 既に起動している場合は何もしない
        if (serverJob?.isActive == true) return

        // サーバーインスタンス
        mWebSocketServer = MyWebsocketServer()

        // サーバー起動処理（バックグラウンドスレッド）
        serverJob = viewModelScope.launch(Dispatchers.IO) {
            // 状態更新（起動中）
            _isServerRunning.value = MyWebsocketServerStatus.CONNECTED

            try {
                // 起動
                Log.i("MyWebsocketServer", "Server starting...")
                mWebSocketServer?.start()
                Log.i("MyWebsocketServer", "Server stopped.")

            } catch (e: Exception) {
                // 例外発生時
                Log.e("MyWebsocketServer", "Server crashed.", e)

            } finally {
                // 停止
                mWebSocketServer = null

                // 状態更新（停止中）
                _isServerRunning.value = MyWebsocketServerStatus.DISCONNECTED
            }
        }

        // 接続中ユーザーリスト監視を開始（サーバーとは別スレッド）
        viewModelScope.launch(Dispatchers.IO) {
            mWebSocketServer?.userList?.collect { newList ->
                _connectionUserList.value = newList
            }
        }
    }

    /**
     * WebSocketサーバー停止関数
     */
    fun stopServer() {
        val server = mWebSocketServer

        if (server != null) {
            // サーバー停止処理（バックグラウンドスレッド）
            viewModelScope.launch(Dispatchers.IO) {
                // 状態更新（停止処理中）
                _isServerRunning.value = MyWebsocketServerStatus.CLOSING

                // サーバーシャットダウン処理が中断されないことを保証
                withContext(NonCancellable) {

                    try {
                        // 接続中の全クライアントへ切断を通知 明示的にセッションクローズ
                        val closeJobs = server.connections.value.map { session ->
                            async { // 並列実行
                                try {
                                    withTimeout(500) {
                                        // WebSocketクローズフレーム送信
                                        session.session.close(CloseReason(CloseReason.Codes.GOING_AWAY, "Server is shutting down."))
                                    }

                                } catch (e: Exception) {
                                    // タイムアウトや切断エラーは無視
                                }
                            }
                        }

                        // 全てのクローズJobの完了を待つ
                        closeJobs.awaitAll()

                        // サーバー停止（ブロッキング関数終了）
                        server.stop()
                        delay(500)  // 競合防止

                    } catch (e: Exception) {
                        // 例外発生時
                        Log.e("MyWebsocketServer", "An error occurred during server shutdown processing.", e)

                    } finally {
                        // ★ サーバー停止を待ってから実行 ★
                        // インスタンス参照クリア
                        mWebSocketServer = null

                        // 実行中のコルーチンをキャンセル
                        serverJob?.cancelAndJoin()  // 確実に終了するのを待つ
                        serverJob = null

                        // 状態更新（停止中）
                        _isServerRunning.value = MyWebsocketServerStatus.DISCONNECTED
                        Log.i("MyWebsocketServer", "Server shutdown.")
                    }
                }
            }
        }
        else {
            // コルーチンキャンセル＆クリア（念のため）
            serverJob?.cancel()
            serverJob = null
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}