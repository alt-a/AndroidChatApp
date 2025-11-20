package com.example.chatappclient.data.model

import kotlinx.serialization.Serializable

/**
 * 接続中ユーザー情報データクラス
 */
@Serializable
data class ConnectionUser(
    val id: Int,
    val name: String
)