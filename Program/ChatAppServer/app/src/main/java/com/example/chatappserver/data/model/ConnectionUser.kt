package com.example.chatappserver.data.model

import kotlinx.serialization.Serializable

/**
 * 接続中ユーザー情報 データクラス
 * @property id     : ユーザーID
 * @property name   : ユーザー名
 */
@Serializable
data class ConnectionUser(
    val id: Int,
    val name: String
)