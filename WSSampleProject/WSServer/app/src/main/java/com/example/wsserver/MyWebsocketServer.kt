package com.example.wsserver

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import java.time.Duration

class MyWebsocketServer {
    // netty←サーバー全体の構成
    private val netty = embeddedServer(Netty, port = 8000) { // エンジン：Netty、ポート：8000
        install(WebSockets) { // Websocketインストール
            timeout = Duration.ofSeconds(5) // タイムアウトの条件
            pingPeriod = Duration.ofMinutes(1) // ping確認の間隔
        }
        routing { // サーバーアクセスの処理
            webSocket("/") { // ルート
                val uniqueId = generateNonce() // クライアントにユニークID発行
                // 接続時
                println("Connection to $uniqueId established")
                // エコー
                // 受信パイプ
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        println("Receive Message: ${frame.readText()}")
                        // 送信パイプ
                        outgoing.send(Frame.Text("Receive Message: ${frame.readText()}"))
                    }
                }
                println("Connection to $uniqueId closed")
            }
        }
    }

    fun start() {
        netty.start(wait = true)
    }

    fun stop() {
        netty.stop()
    }
}

// PC上で直接実行するための関数
fun main() {
    MyWebsocketServer().start()
}