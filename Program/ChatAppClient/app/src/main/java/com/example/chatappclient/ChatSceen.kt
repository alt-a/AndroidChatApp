package com.example.chatappclient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons // ★追加
import androidx.compose.material.icons.filled.ExitToApp // ★追加
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.* // ★ (Button, Text などまとめて import)
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * チャット画面
 * @param viewModel 共有する ChatViewModel
 * @param onDisconnect 切断時に呼び出されるコールバック (画面遷移用)
 */
@Composable
fun ClientChatScreen(
    viewModel: ChatViewModel,
    onDisconnect: () -> Unit // ★コールバックを受け取る
) {
    // ViewModelが保持しているメッセージリストを監視
    // (messages.value が更新されると、 'messages' も自動的に更新 = 再描画)
    val messages by viewModel.messages.collectAsState()

    // ViewModelが保持している自分の名前
    val myName by viewModel.userName.collectAsState()

    // ViewModelが保持している接続状態の監視
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    val uiState = ClientChatScreenUIState(
        messages = messages,
        userName = myName,
        connectionStatus = connectionStatus
    )

    // --- 要望2: 接続が切れたら自動で戻る ---
    LaunchedEffect(connectionStatus) {
        if (connectionStatus != "Connected" && connectionStatus != "Connecting...") {
            // 接続状態が "Connected" 以外 (Error, Disconnected など) になったら
            // ログイン画面に戻る
            onDisconnect()
        }
    }

    // ステートレスUIコンポーネント
    ClientChatScreenContent(
        uiState = uiState,
        onDisconnect = viewModel::disconnect,
        onDisconnectButtonClick = onDisconnect,
        onSendMessageButtonClick = viewModel::sendMessage
    )
}

/**
 * チャットバブル
 * @param msg       : チャットメッセージ
 * @param isMine    : 送信メッセージ（true）、受信メッセージ（false）
 */
@Composable
fun ChatBubble(msg: ChatMessage, isMine: Boolean) {
    // 送信・受信で吹き出し方向を変更
    val sendBubbleShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 0.dp    // 直角
    )
    val receiveBubbleShape = RoundedCornerShape(
        topStart = 0.dp,    // 直角
        topEnd = 10.dp,
        bottomStart = 10.dp,
        bottomEnd = 10.dp
    )

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 4.dp)
            .wrapContentWidth(
                align =
                    if (isMine) Alignment.End   // 送信メッセージ: 画面右側に表示
                    else Alignment.Start        // 受信メッセージ: 画面左側に表示
            )
    ) {
        // ユーザー名
        if (isMine == false) {
            Text(
                text = msg.user,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // メッセージ本文
        Surface(
            color =
                if (isMine) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onPrimary,
            shape =
                if (isMine) sendBubbleShape
                else receiveBubbleShape,
            shadowElevation = 3.dp
        ) {
            Text(
                text = msg.message,
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (isMine) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    vertical = 11.dp,
                    horizontal = 15.dp
                )
            )
        }
    }
}

/**
 * チャット画面 ViewModel保持データ表示用データクラス
 */
data class ClientChatScreenUIState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            user = "ユーザー1",
            message = "こんにちは。"
        ),
        ChatMessage(
            user = "ユーザー2",
            message = "おつかれさまです。"
        )
    ),
    val userName: String = "ユーザー1",
    val connectionStatus: String = "Disconnected"
)

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .imePadding()               // ソフトキーボード表示時に押し上げる
                        .navigationBarsPadding(),   // システムナビゲーションバーの高さ分押し上げる
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(
                            text = "メッセージ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }
    ) { paddingValues -> // ★Scaffold が計算した padding

        // --- メッセージリスト ---
        LazyColumn(
            state = listState,
            modifier = Modifier
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
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment =
                        if (isMine) Alignment.CenterEnd // 送信メッセージ: 画面右側に表示
                        else Alignment.CenterStart      // 受信メッセージ: 画面左側に表示
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