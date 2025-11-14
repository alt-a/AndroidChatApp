package com.example.chatappserver.ui.screen.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * アプリ起動時画面 ステートレスUIコンポーネント
 * @param onStartup : 「はじめる」ボタン押下時 画面遷移用コールバック
 */
@Composable
fun ServerStartScreenContent(
    onStartup: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
        ) {
            Text(
                text = "チャットアプリ",
                fontSize = 36.sp,
                modifier = Modifier.Companion.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "サーバー",
                fontSize = 36.sp,
                modifier = Modifier.Companion.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.Companion.height(48.dp))
            ExtendedFloatingActionButton(
                onClick = {
                    // ホーム画面へ遷移
                    onStartup()
                },
                modifier = Modifier.Companion.padding(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "はじめる",
                    fontSize = 22.sp,
                    modifier = Modifier.Companion.padding(
                        vertical = 16.dp,
                        horizontal = 28.dp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ServerStartScreenContentPreview() {
    ServerStartScreenContent(
        onStartup = {}
    )
}