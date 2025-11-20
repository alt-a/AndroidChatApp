package com.example.chatappclient.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * フレーム内容識別子 シールドインターフェース
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("content")
sealed interface FrameID