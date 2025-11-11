package com.example.chatappclient.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chatappclient.ChatMessage

/**
 * チャットバブル
 * @param msg       : チャットメッセージ
 * @param isMine    : 送信メッセージ（true）、受信メッセージ（false）
 */
@Composable
fun ChatBubble(msg: ChatMessage, isMine: Boolean) {
    // 送信・受信で吹き出し方向を変更
    val sendBubbleShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 0.dp    // 直角
    )
    val receiveBubbleShape = RoundedCornerShape(
        topStart = 0.dp,    // 直角
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 10.dp
    )

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.Companion
            .fillMaxWidth(0.8f)
            .padding(vertical = 4.dp)
            .wrapContentWidth(
                align =
                    if (isMine) Alignment.Companion.End   // 送信メッセージ: 画面右側に表示
                    else Alignment.Companion.Start        // 受信メッセージ: 画面左側に表示
            )
    ) {
        // ユーザー名
        if (isMine == false) {
            Text(
                text = msg.user,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Companion.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // メッセージ本文
        Surface(
            color =
                if (isMine) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onPrimary,
            shape =
                if (isMine) sendBubbleShape
                else receiveBubbleShape,
            shadowElevation = 3.dp
        ) {
            Text(
                text = msg.message,
                textAlign = TextAlign.Companion.Left,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (isMine) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                modifier = Modifier.Companion.padding(
                    vertical = 11.dp,
                    horizontal = 15.dp
                )
            )
        }
    }
}