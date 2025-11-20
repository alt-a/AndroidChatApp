package com.example.chatappserver.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 接続中ユーザー一覧フレーム データクラス
 * @property list   : 接続中ユーザーリスト
 */
@Serializable
@SerialName("connection_user_list")
data class ConnectionUserList(
    val list: List<ConnectionUser>
) : FrameID