package com.example.chatappclient.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ユーザー名通知フレーム データクラス
 * @property name   : ユーザー名
 */
@Serializable
@SerialName("user_name")
data class UserName(
    val name: String
) : FrameID