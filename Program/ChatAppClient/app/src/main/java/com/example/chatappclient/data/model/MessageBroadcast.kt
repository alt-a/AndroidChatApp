package com.example.chatappclient.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * サーバーと送受信するJSONチャットメッセージのデータ構造。
 * @Serializable アノテーションを付けることで、
 * kotlinx.serialization がこのクラスとJSON文字列を相互変換できるようになる。
 */
@Serializable
@SerialName("message_broadcast")
data class MessageBroadcast(
    val user: String,    // 送信者の名前 (例: "Hirayama")
    val message: String  // メッセージ本文 (例: "こんにちは")
) : FrameID