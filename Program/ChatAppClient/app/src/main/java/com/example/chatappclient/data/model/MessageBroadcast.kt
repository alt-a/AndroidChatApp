package com.example.chatappclient.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ブロードキャストメッセージ送信フレーム データクラス
 * @property from       : メッセージ送信元 ユーザーID
 * @property message    : メッセージ
 * @property timestamp  : メッセージ送信時刻（Unix時間）
 */
@Serializable
@SerialName("message_broadcast")
data class MessageBroadcast(
    @SerialName("id_from") val from: Int,
    val message: String,
    val timestamp: Long
) : FrameID