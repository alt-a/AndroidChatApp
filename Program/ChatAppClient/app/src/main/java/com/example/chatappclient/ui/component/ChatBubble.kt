package com.example.chatappclient.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chatappclient.data.model.MessageToYou
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * チャットバブル
 * @param msg       : チャットメッセージ
 * @param isMine    : 送信メッセージ（true）、受信メッセージ（false）
 */
@Composable
fun ChatBubble(msg: MessageToYou, name: String, isMine: Boolean) {
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

    // 送信時刻文字列取得
    val instant = Instant.ofEpochSecond(msg.timestamp)
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("H:mm", Locale.JAPAN)
    val sendTime = zonedDateTime.format(formatter)

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.Companion
            .fillMaxWidth()             // 横幅を画面いっぱいに広げる
            .padding(vertical = 4.dp)   // 垂直方向に余白
            .wrapContentWidth(
                align =
                    if (isMine) Alignment.Companion.End   // 送信メッセージ: 画面右側に表示
                    else Alignment.Companion.Start        // 受信メッセージ: 画面左側に表示
            )
    ) {
        // 送信側 送信時刻
        if (isMine) {
            Text(
                text = sendTime,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.Companion
                // 送信時刻表示の幅を尊重し、残りのスペースをすべて割り当てる
                .weight(1f, fill = false)
                // 最大幅を画面サイズの70%に制限
                .sizeIn(maxWidth = LocalConfiguration.current.screenWidthDp.dp * 0.7f)
        ) {
            // ユーザー名
            if (isMine == false) {
                Text(
                    text = name,
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
        // 受信側 送信時刻
        if (!isMine) {
            Text(
                text = sendTime,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}