package com.example.chatappserver.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatappserver.data.websocket.MyWebsocketServerManager
import com.example.chatappserver.ui.component.ConnectionUserCard
import com.example.chatappserver.ui.component.StopServerConfirmAlert

/**
 * ホーム画面
 * @param ipAddress : IPアドレステキスト
 * @param onStop    : 画面遷移用コールバック
 */
@Composable
fun ServerHomeScreen(
    viewModel: MyWebsocketServerManager,
    ipAddress: String,
    onStop: () -> Unit
) {
    // ステートレスUIコンポーネント
    ServerHomeScreenContent(
        ipAddress = ipAddress,
        onStop = onStop
    )
}