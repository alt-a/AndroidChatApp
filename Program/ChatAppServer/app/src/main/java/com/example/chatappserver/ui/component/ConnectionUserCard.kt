package com.example.chatappserver.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 接続中ユーザー情報表示Card
 */
@Composable
fun ConnectionUserCard(text: String) {
    Card(
        modifier = Modifier.Companion.padding(vertical = 2.dp).fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.Companion.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                Text(
                    text = "IPアドレス：",
                    fontWeight = FontWeight.Companion.Bold
                )
                Text(text = text)
            }
            Spacer(modifier = Modifier.Companion.height(4.dp))
            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                Text(
                    text = "PORT：",
                    fontWeight = FontWeight.Companion.Bold
                )
                // ポート番号を 8080 に変更
                Text(text = "8080")
            }
        }
    }
}