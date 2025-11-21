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
import com.example.chatappclient.data.model.MessageBroadcast
import com.example.chatappclient.data.model.MessageToYou

/**
 * チャットバブル
 * @param msg       : チャットメッセージ
 * @param isMine    : 送信メッセージ（true）、受信メッセージ（false）
 */
@Composable
fun ChatBubble(msg: MessageToYou, isMine: Boolean) {
    // 送信・受信で吹き出し方向を変更
    val sendBubbleShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 0.dp    // 送信メッセージ: 右下を直角にする
    )
    val receiveBubbleShape = RoundedCornerShape(
        topStart = 0.dp,    // 受信メッセージ: 左上を直角にする
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 10.dp
    )

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.Companion
            .fillMaxWidth(0.8f) // チャットバブルの横幅を最大サイズの80%に制限
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
                text = msg.from.toString(), // TODO: MessageBroadcast 内容変更による暫定対応
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Companion.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // メッセージ本文
        Surface(
            color =
                // 送信・受信でチャットバブル背景色を反転
                if (isMine) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onPrimary,
            shape =
                if (isMine) sendBubbleShape
                else receiveBubbleShape,
            shadowElevation = 3.dp  // 影を付ける
        ) {
            Text(
                text = msg.message,
                textAlign = TextAlign.Companion.Left,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    // 送信・受信で文字色を反転
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