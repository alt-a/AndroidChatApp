package com.example.chatappclient.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatappclient.data.websocket.MyWebsocketClientStatus

/**
 * サーバー切断アラート
 * @param reason    : WebSocket接続状態
 * @param onDismiss : 画面遷移用コールバック
 */
@Composable
fun DisconnectAlert(
    reason: MyWebsocketClientStatus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = ""
            )
        },
        text = {
            var reasonMessage = ""
            if (reason == MyWebsocketClientStatus.DISCONNECTED_ERROR) {
                // エラー終了
                reasonMessage = "ネットワークエラー発生。"
            }
            else if (reason == MyWebsocketClientStatus.SEND_ERROR) {
                // 送信失敗
                reasonMessage = "メッセージの送信に失敗しました。"
            }
            else if (reason == MyWebsocketClientStatus.DISCONNECTED_SERVER_CLOSE) {
                // サーバー終了
                reasonMessage = "サーバーが終了しました。"
            }
            Text(
                text = "${reasonMessage}\n起動時画面に戻ります。",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "OK",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun DisconnectAlertPreview() {
    DisconnectAlert(
        reason = MyWebsocketClientStatus.DISCONNECTED_ERROR,
        onDismiss = {}
    )
}