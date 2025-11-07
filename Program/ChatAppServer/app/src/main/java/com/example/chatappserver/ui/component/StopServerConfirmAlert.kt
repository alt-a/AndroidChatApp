package com.example.chatappserver.ui.component

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

@Composable
fun StopServerConfirmAlert(
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
                    text = "サーバーを終了します。"
                )
                Text(
                    text = "よろしいですか？"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "OK",
                    fontWeight = FontWeight.Companion.Bold
                )
            }
        },
        dismissButton = {
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
fun StopServerConfirmAlertPreview() {
    StopServerConfirmAlert(
        onConfirm = {},
        onDismissRequest = {}
    )
}