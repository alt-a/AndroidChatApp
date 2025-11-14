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
import com.example.chatappserver.data.websocket.MyWebsocketServerManager

/**
 * アプリ起動時画面
 * @param viewModel : 共有するViewModel
 * @param onStartup : 「はじめる」ボタン押下時 画面遷移用コールバック
 */
@Composable
fun ServerStartScreen(
    viewModel: MyWebsocketServerManager,
    onStartup: () -> Unit
) {
    // ステートレスUIコンポーネント
    ServerStartScreenContent(
        onStartup = onStartup
    )
}