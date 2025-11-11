package com.example.chatappclient.ui.screen.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatappclient.ui.component.ChatBubble

/**
 * チャット画面 ステートレスUIコンポーネント
 * @param uiState                   : ViewModelが保持するUIデータ
 * @param onDisconnect              : サーバー切断関数
 * @param onDisconnectButtonClick   : 画面遷移用コールバック関数
 * @param onSendMessageButtonClick  : メッセージ送信関数
 */
@OptIn(ExperimentalMaterial3Api::class) // ★TopAppBar用に必要
@Composable
fun ClientChatScreenContent(
    uiState: ClientChatScreenUIState,
    onDisconnect: () -> Unit,
    onDisconnectButtonClick: () -> Unit,
    onSendMessageButtonClick: (message: String) -> Unit
) {
    // 画面下部のメッセージ入力欄用の状態変数
    var messageText by remember { mutableStateOf("") }

    // スクロール状態を管理
    val listState = rememberLazyListState()

    // messagesリストの中身が変わるたびに、一番下までスクロールする
    LaunchedEffect(uiState.messages) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(index = uiState.messages.size - 1)
        }
    }

    // ★画面全体を Scaffold で囲む
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "チャット (${uiState.connectionStatus})") }, // 接続状態を表示
                navigationIcon = {
                    // --- 要望1: 接続画面に戻るボタン ---
                    IconButton(onClick = {
                        onDisconnect()
                        onDisconnectButtonClick()
//                        viewModel.disconnect() // ViewModel に切断を通知
                        // onDisconnect() // LaunchedEffect が検知するので不要
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "切断",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        // ★メッセージ送信欄 (画面下部) を Scaffold の bottomBar に移動
        bottomBar = {
            Surface(
                // 背景色設定
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(8.dp)
                        .imePadding()               // ソフトキーボード表示時に押し上げる
                        .navigationBarsPadding(),   // システムナビゲーションバーの高さ分押し上げる
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.Companion.weight(1f),
                        placeholder = {
                            Text(
                                text = "メッセージ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Spacer(modifier = Modifier.Companion.width(8.dp))
                    IconButton(
                        onClick = {
                            onSendMessageButtonClick(messageText)
                            messageText = ""
                        },
                        enabled = messageText.isNotBlank() && uiState.connectionStatus == "Connected",
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "",
                            modifier = Modifier.Companion.size(28.dp),
                        )
                    }
                }
            }
        }
    ) { paddingValues -> // ★Scaffold が計算した padding

        // --- メッセージリスト ---
        LazyColumn(
            state = listState,
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues), // ★Scaffold の padding を適用
            contentPadding = PaddingValues(
                vertical = 10.dp,
                horizontal = 15.dp
            )
        ) {
            items(uiState.messages) { msg ->
                val isMine = (msg.user == uiState.userName)
                Box(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    contentAlignment =
                        if (isMine) Alignment.Companion.CenterEnd // 送信メッセージ: 画面右側に表示
                        else Alignment.Companion.CenterStart      // 受信メッセージ: 画面左側に表示
                ) {
                    ChatBubble(msg = msg, isMine = isMine)
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ClientChatScreenContentPreview() {
    ClientChatScreenContent(
        uiState = ClientChatScreenUIState(),
        onDisconnect = {},
        onDisconnectButtonClick = {},
        onSendMessageButtonClick = {}
    )
}