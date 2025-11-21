package com.example.chatappserver.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 個別メッセージ受信フレーム データクラス
 * @property to         : メッセージ送信先 ユーザーID
 * @property from       : メッセージ送信元 ユーザーID
 * @property message    : メッセージ
 * @property timestamp  : メッセージ送信時刻（Unix時間）
 */
@Serializable
@SerialName("message_specified")
data class MessageSpecified(
    @SerialName("id_to") val to: List<Int>,
    @SerialName("id_from") val from: Int,
    val message: String,
    val timestamp: Long
) : FrameID