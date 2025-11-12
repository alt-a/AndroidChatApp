package com.example.chatappclient.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * チャット終了確認アラート
 * @param onDismissRequest  : 「戻る」ボタン押下時 画面遷移用コールバック
 * @param onConfirm         : 「OK」ボタン押下時 画面遷移用コールバック
 */
@Composable
fun EndChatConfirmAlert(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = ""
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "チャットを終了します。"
                )
                Text(
                    text = "よろしいですか？"
                )
            }
        },
        confirmButton = {   // "OK"
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "OK",
                    fontWeight = FontWeight.Companion.Bold
                )
            }
        },
        dismissButton = {   // "戻る"
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = "戻る"
                )
            }
        },
        onDismissRequest = onDismissRequest
    )
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun EndChatConfirmAlertPreview() {
    EndChatConfirmAlert(
        onConfirm = {},
        onDismissRequest = {}
    )
}