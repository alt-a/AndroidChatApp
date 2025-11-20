package com.example.chatappclient.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ユーザーID通知フレーム データクラス
 * @property id : ユーザーID
 */
@Serializable
@SerialName("user_id")
data class UserID(
    val id: Int
) : FrameID