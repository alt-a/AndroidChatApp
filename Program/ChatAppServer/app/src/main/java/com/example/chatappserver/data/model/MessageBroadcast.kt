package com.example.chatappserver.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ブロードキャストメッセージフレーム データクラス
 */
@Serializable
@SerialName("message_broadcast")
data class MessageBroadcast(
    val user: String,
    val message: String
) : FrameID