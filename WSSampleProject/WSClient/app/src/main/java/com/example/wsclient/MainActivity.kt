package com.example.wsclient // ← パッケージ名が wsclient になっていることを確認

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.wsclient.ui.theme.WSClientTheme // ← ここも WSClient になっているはず

class MainActivity : ComponentActivity() {

    // 1. ViewModel をインスタンス化
    // "by viewModels()" を使うと、画面回転などでもViewModelが保持される
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 2. M3のテーマでアプリ全体を囲う
            WSClientTheme { // ← ここの名前が Refactor されていればOK

                // 3. ViewModel から最新の状態を受け取る
                // .collectAsState() を使うと、StateFlow の値が変わるたびに
                // Composeが自動で再描画される
                val connectionStatus by viewModel.connectionStatus.collectAsState()
                val messages by viewModel.messages.collectAsState()

                // 4. UIの Composable 関数を呼び出す
                WebSocketClientScreen(
                    connectionStatus = connectionStatus,
                    messages = messages,
                    onConnect = { ip -> viewModel.connect(ip) }, // 接続
                    onDisconnect = { viewModel.disconnect() }, // 切断
                    onSendMessage = { msg -> viewModel.sendMessage(msg) } // 送信
                )
            }
        }
    }
}

// --- ここから下がUIの本体です ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSocketClientScreen(
    connectionStatus: String,
    messages: List<String>,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    // UI内で一時的に使う状態（入力テキストなど）
    var ipAddressText by remember { mutableStateOf(TextFieldValue("127.0.0.1")) }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    // メッセージリストが更新されたら自動で一番下までスクロールするための設定
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("WebSocket Client") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // --- 接続ステータス ---
            Text(
                text = "ステータス: $connectionStatus",
                style = MaterialTheme.typography.titleMedium
            )

            // --- IPアドレス入力と接続/切断ボタン ---
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ipAddressText,
                    onValueChange = { ipAddressText = it },
                    label = { Text("IP Address") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (connectionStatus == "未接続" || connectionStatus.startsWith("切断")) {
                            onConnect(ipAddressText.text)
                        } else {
                            onDisconnect()
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    // ボタンの文字を接続状態で変える
                    val buttonText = if (connectionStatus == "未接続" || connectionStatus.startsWith("切断")) "接続" else "切断"
                    Text(buttonText)
                }
            }

            // --- メッセージ履歴 ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // 残りのスペースをすべて使う
                state = listState // スクロール状態を紐付け
            ) {
                items(messages) { msg ->
                    Text(msg, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- メッセージ入力と送信ボタン ---
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("メッセージ") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (messageText.text.isNotBlank()) {
                            onSendMessage(messageText.text)
                            messageText = TextFieldValue("") // 送信したら入力欄を空にする
                        }
                    },
                    enabled = connectionStatus == "接続済み", // 接続中だけ押せる
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("送信")
                }
            }
        }
    }
}