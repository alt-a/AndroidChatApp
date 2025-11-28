package com.example.chatappclient.ui.screen.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatappclient.data.websocket.MyWebsocketClientStatus
import com.example.chatappclient.ui.component.ChatBubble
import com.example.chatappclient.ui.component.DisconnectAlert
import com.example.chatappclient.ui.component.EndChatConfirmAlert
import com.example.chatappclient.ui.component.MaxLengthErrorOutlinedTextField
import com.example.chatappclient.ui.component.SelectRecipientDialog

/**
 * チャット画面 ステートレスUIコンポーネント
 * @param uiState                   : ViewModelが保持するUIデータ
 * @param onDisconnect              : サーバー切断関数
 * @param onDisconnectButtonClick   : 画面遷移用コールバック関数
 * @param onSendMessageBroadcast    : ブロードキャストメッセージ送信関数
 * @param onSendMessageSpecified    : 個別メッセージ送信関数
 */
@OptIn(ExperimentalMaterial3Api::class) // ★TopAppBar用に必要
@Composable
fun ClientChatScreenContent(
    uiState: ClientChatScreenUIState,
    onDisconnect: () -> Unit,
    onDisconnectButtonClick: () -> Unit,
    onRequest: () -> Unit,
    onSendMessageBroadcast: (message: String) -> Unit,
    onSendMessageSpecified: (sendList: List<Int>, message: String) -> Unit
) {
    // 画面下部のメッセージ入力欄用の状態変数
    var messageText by remember { mutableStateOf("") }

    // 最大入力文字数
    val maxLength = 300

    // チャット終了確認アラート表示状態管理
    val showEndChatConfirmAlert = remember { mutableStateOf(false) }

    // 送信先選択ダイアログ表示状態管理
    val showSelectRecipientDialog = remember { mutableStateOf(false) }

    // サーバー切断アラート表示状態管理
    val showDisconnectAlert = remember { mutableStateOf(false) }

    // 接続状態がエラーになったらアラート表示
    LaunchedEffect(uiState.connectionStatus) {
        if (MyWebsocketClientStatus.ERROR.ordinal <= uiState.connectionStatus.ordinal) {
            showDisconnectAlert.value = true
        }
    }

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
                        showEndChatConfirmAlert.value = true  // チャット終了確認アラート表示
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
                Column(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(8.dp)
                        .imePadding()               // ソフトキーボード表示時に押し上げる
                        .navigationBarsPadding()    // システムナビゲーションバーの高さ分押し上げる
                ) {
                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                    ) {
                        MaxLengthErrorOutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            maxLength = maxLength,
                            modifier = Modifier.Companion
                                .weight(1f)
                                .heightIn(max = 164.dp),
                            placeholder = {
                                Text(
                                    text = "メッセージ",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.Companion.width(8.dp))
                        IconButton(
                            onClick = {
                                // 接続中ユーザー情報要求送信
                                onRequest()

                                // 送信先選択ダイアログ表示
                                showSelectRecipientDialog.value = true
                            },
                            enabled = (messageText.isNotBlank() && messageText.length <= maxLength && uiState.connectionStatus == MyWebsocketClientStatus.CONNECTED),
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

                    // 入力文字数上限超過時 エラーメッセージ表示
                    if (messageText.length > maxLength) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "文字数制限を",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${messageText.length - maxLength}文字超過",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "しており送信できません",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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
                // 自分が送信したメッセージ
                val isMine = (msg.from == uiState.myUserID)

                // 送信元ユーザー名を取得
                val nameMap: Map<Int, String> = uiState.connectionUserList.associate { it.id to it.name }
                val name = nameMap.getOrElse(msg.from) { "Unknown User" }

                Box(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    contentAlignment =
                        if (isMine) Alignment.Companion.CenterEnd // 送信メッセージ: 画面右側に表示
                        else Alignment.Companion.CenterStart      // 受信メッセージ: 画面左側に表示
                ) {
                    ChatBubble(msg = msg, name = name, isMine = isMine)
                }
            }
        }
    }

    // ----- 送信先選択ダイアログ表示 -----
    if (showSelectRecipientDialog.value) {
        SelectRecipientDialog(
            recipientsList = uiState.connectionUserList,
            onDismissRequest = { showSelectRecipientDialog.value = false },    // "戻る"
            onSendBroadcast = {
                // "送信"（ブロードキャスト）
                onSendMessageBroadcast(messageText)
                messageText = ""
                showSelectRecipientDialog.value = false    // ダイアログを閉じる
            },
            onSendSpecified = { sendList ->
                // "送信"（個別に送信）
                onSendMessageSpecified(sendList, messageText)
                messageText = ""
                showSelectRecipientDialog.value = false    // ダイアログを閉じる
            }
        )
    }

    // ----- チャット終了確認アラート表示 -----
    if (showEndChatConfirmAlert.value) {
        EndChatConfirmAlert(
            onDismissRequest = { showEndChatConfirmAlert.value = false }, // "戻る"
            onConfirm = {   // "OK"
                showEndChatConfirmAlert.value = false
                onDisconnect()              // ViewModel に切断を通知
                onDisconnectButtonClick()   // 起動時画面に戻る
            }
        )
    }

    // ----- サーバー切断アラート表示 -----
    if (showDisconnectAlert.value) {
        DisconnectAlert(
            reason = uiState.connectionStatus,
            onDismiss = {   // "OK"
                showDisconnectAlert.value = false
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
        onSendMessageBroadcast = {},
        onSendMessageSpecified = { sendList, message ->
            // 何もしない
        }
    )
}