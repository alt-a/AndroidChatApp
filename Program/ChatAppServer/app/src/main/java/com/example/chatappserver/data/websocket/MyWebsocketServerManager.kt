package com.example.chatappserver.data.websocket

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    // 起動中のコルーチンジョブを保持するための変数
    private var serverJob: Job? = null

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
            _isServerRunning.value = true

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
                _isServerRunning.value = false
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

                // サーバーシャットダウン処理が中断されないことを保証
                withContext(NonCancellable) {

                    try {
                        // 接続中の全クライアントへ切断を通知 明示的にセッションクローズ
                        val closeJobs = server.connections.map { session ->
                            async { // 並列実行
                                try {
                                    withTimeout(500) {
                                        // WebSocketクローズフレーム送信
                                        session.close(CloseReason(CloseReason.Codes.GOING_AWAY, "Server is shutting down."))
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