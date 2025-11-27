package com.example.chatappserver.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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

/**
 * IPアドレス表示ダイアログ
 * @param ipAddress : IPアドレス文字列
 * @param onDismiss : ダイアログを閉じるコールバック
 */
@Composable
fun IpAddressDialog(
    ipAddress: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = ""
            )
        },
        title = {
            Text(
                text = "現在のIPアドレス",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = ipAddress,
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
                    text =  "OK",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun IpAddressDialogPreview() {
    IpAddressDialog(
        ipAddress = "192.168.11.17",
        onDismiss = {}
    )
}