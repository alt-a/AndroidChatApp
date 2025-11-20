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
import com.example.chatappclient.data.websocket.MyWebsocketClientStatus
import com.example.chatappclient.ui.component.ChatBubble
import com.example.chatappclient.ui.component.EndChatConfirmAlert
import com.example.chatappclient.ui.component.SelectRecipientDialog

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
    onRequest: () -> Unit,
    onSendMessageButtonClick: (message: String) -> Unit
) {
    // 画面下部のメッセージ入力欄用の状態変数
    var messageText by remember { mutableStateOf("") }

    // アラート表示状態管理
    val showAlert = remember { mutableStateOf(false) }

    // 送信先選択ダイアログ表示状態管理
    val showDialog = remember { mutableStateOf(false) }

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
                title = { Text(text = "チャット (${uiState.connectionStatus.text})") }, // 接続状態を表示
                navigationIcon = {  // 戻るボタン
                    IconButton(onClick = {
                        showAlert.value = true  // チャット終了確認アラート表示
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
                            // 接続中ユーザー情報要求送信
                            onRequest()

                            // 送信先選択ダイアログ表示
                            showDialog.value = true
                        },
                        enabled = messageText.isNotBlank() && uiState.connectionStatus == MyWebsocketClientStatus.CONNECTED,
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

    // ----- 送信先選択ダイアログ表示 -----
    if (showDialog.value) {
        SelectRecipientDialog(
            recipientsList = uiState.connectionUserList,
            onDismissRequest = { showDialog.value = false },    // "戻る"
            onSend = {  // "送信"
                onSendMessageButtonClick(messageText)
                messageText = ""
                showDialog.value = false
            }
        )
    }

    // ----- チャット終了確認アラート表示 -----
    if (showAlert.value) {
        EndChatConfirmAlert(
            onDismissRequest = { showAlert.value = false }, // "戻る"
            onConfirm = {   // "OK"
                showAlert.value = false
                onDisconnect()              // ViewModel に切断を通知
                onDisconnectButtonClick()   // 起動時画面に戻る
            }
        )
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ClientChatScreenContentPreview() {
    ClientChatScreenContent(
        uiState = ClientChatScreenUIState(),
        onDisconnect = {},
        onDisconnectButtonClick = {},
        onRequest = {},
        onSendMessageButtonClick = {}
    )
}