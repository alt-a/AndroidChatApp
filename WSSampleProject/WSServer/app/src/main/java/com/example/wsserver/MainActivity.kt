package com.example.wsserver

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import java.net.Inet4Address
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private var mWebSocketServer: MyWebsocketServer? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. IPアドレスを保持するための状態(State)変数を用意
        // 値が変わると再描画できるMutableState
        val ipAddressState = mutableStateOf("Detecting IP...")

        // 2. IPアドレスを監視して、変更があればStateを更新
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        // IPアドレスの取得と監視
        try {
            // 現在アクティブなネットワーク取得
            val currentNetwork = manager.activeNetwork
            // ネットワークの詳細取得
            val linkProperties = manager.getLinkProperties(currentNetwork)
            // IPアドレスリストから該当する最初の一つを探す
            if (linkProperties != null) {
                val initialIp = linkProperties.linkAddresses
                    .firstOrNull { it.address is Inet4Address && it.address.isSiteLocalAddress }
                    ?.address?.hostAddress
                if (initialIp != null) {
                    // IPアドレスが見つかればStateに書き込み
                    ipAddressState.value = initialIp
                    Log.d("NetworkCallback", "Initial IP: $initialIp") // 最初のIPアドレス出力
                } else {
                    Log.d("NetworkCallback", "Initial IP not found in active network") // IPアドレス不明
                }
            } else {
                Log.d("NetworkCallback", "No active network found initially") // アクティブなネットなし
            }
        }
        catch (e: SecurityException) {
            Log.e("NetworkCallback", "Permission missing for initial check?", e)
            ipAddressState.value = "Permission Error?" // エラー表示
        }

        // ネットワークの状態変化監視
        // 見張り番オブジェクト
        // 本当はonCreateの外に書いてonDestroyで一緒に解除する
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // 状態変化時に自動実行
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                Log.d("NetworkCallback", "LinkProperties changed: $linkProperties") //ネット状態変化
                //最初と同じ処理
                val newIp = linkProperties.linkAddresses
                    .firstOrNull { it.address is Inet4Address && it.address.isSiteLocalAddress }
                    ?.address?.hostAddress ?: "Not Found"
                Log.d("NetworkCallback", "Found IP Address: $newIp") // 新しいIPアドレス出力
                ipAddressState.value = newIp
            }
        }
        // Androidシステムに登録、監視スタート
        manager.registerDefaultNetworkCallback(networkCallback)

        // 3. Jetpack ComposeでUIを構築
        setContent {
            // YourAppTheme { ... } のようなテーマで囲うのが一般的です
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("WebSocket Server") })
                }
            ) { paddingValues ->
                // ipAddressStateの値が変わると、この画面も自動で再描画される
                ServerInfoScreen(
                    ipAddress = ipAddressState.value,
                    padding = paddingValues
                )
            }
        }

        // 4. バックグラウンドでサーバーを起動 (新しい、推奨される方法)
        lifecycleScope.launch { // ← 外側はシンプルなlaunchに
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // このブロックの中の処理は、アプリがCREATED状態以上のときだけ実行され、
                // STOPPED状態になると自動的にキャンセル（中断）されます。
                mWebSocketServer = MyWebsocketServer()
                thread { // Ktorのstart(wait=true)はスレッドをブロックするため、別スレッドで実行
                    mWebSocketServer?.start()
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // サーバーを終了
        thread {
            mWebSocketServer?.stop()
            mWebSocketServer = null
        }
    }
}

// 画面表示用のComposable関数
@Composable
fun ServerInfoScreen(ipAddress: String, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding) // Scaffoldからのpaddingを適用
            .padding(16.dp),  // コンテンツ自体のpadding
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "IPアドレス：",
                fontWeight = FontWeight.Bold
            )
            Text(text = ipAddress)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "PORT：",
                fontWeight = FontWeight.Bold
            )
            Text(text = "8000")
        }
    }
}