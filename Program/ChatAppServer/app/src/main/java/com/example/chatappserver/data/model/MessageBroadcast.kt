package com.example.chatappserver.data.model

import kotlinx.serialization.Serializable

/**
 * ブロードキャストメッセージフレーム データクラス
 */
@Serializable
data class MessageBroadcast(
    val user: String,
    val message: String
)