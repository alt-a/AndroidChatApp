package com.example.chatappserver.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 接続中ユーザー情報要求フレーム オブジェクト
 */
@Serializable
@SerialName("request_connection_user_info")
object RequestConnectionUserInfo : FrameID