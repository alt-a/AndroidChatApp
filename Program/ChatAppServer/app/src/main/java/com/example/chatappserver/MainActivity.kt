package com.example.chatappserver

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.net.Inet4Address
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private var mWebSocketServer: MyWebsocketServer? = null

    // IPアドレスを保持するための状態(State)変数
    private val ipAddressState = mutableStateOf("Detecting IP...")

    // ネットワーク監視用のコールバック
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            Log.d("NetworkCallback", "LinkProperties changed: $linkProperties")
            updateIpAddress(linkProperties)
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            // ネットワークが失われた場合、再度検出を試みる
            // (ただし、すぐ別のネットワークに切り替わるはずなので、ここではシンプルにIP未検出状態にする)
            ipAddressState.value = "Network Lost. Re-detecting..."
            Log.d("NetworkCallback", "Network lost")
            // すぐにアクティブなネットワークを確認し直す
            try {
                val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = manager.activeNetwork
                if (activeNetwork != null) {
                    val props = manager.getLinkProperties(activeNetwork)
                    if (props != null) {
                        updateIpAddress(props)
                    }
                } else {
                    ipAddressState.value = "No Active Network"
                }
            } catch (e: Exception) {
                Log.e("NetworkCallback", "Error re-checking IP on network lost", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. IPアドレスの監視を開始
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            // (a) 現在のIPアドレスを取得
            val currentNetwork = manager.activeNetwork
            val linkProperties = manager.getLinkProperties(currentNetwork)
            if (linkProperties != null) {
                updateIpAddress(linkProperties)
            } else {
                ipAddressState.value = "No active network"
                Log.d("NetworkCallback", "No active network found initially")
            }
            // (b) ネットワーク状態変化の監視を開始
            manager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: SecurityException) {
            Log.e("NetworkCallback", "Permission missing for initial check?", e)
            ipAddressState.value = "Permission Error?"
        } catch (e: Exception) {
            Log.e("NetworkCallback", "Error during initial IP check", e)
            ipAddressState.value = "Detection Error"
        }


        // 2. Jetpack ComposeでUIを構築
        setContent {
            // YourAppTheme { ... } のようなテーマで囲うのが一般的です
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                ChatAppServerNavigation(ipAddressState.value)
            }
        }

        // 3. バックグラウンドでサーバーを起動 (推奨される方法)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // このブロックは、ActivityがCREATED状態以上のときだけ実行され、
                // STOPPED状態になると自動的にキャンセル（中断）されます。
                mWebSocketServer = MyWebsocketServer()
                thread { // Ktorのstart(wait=true)はスレッドをブロックするため、別スレッドで実行
                    try {
                        Log.i("MyWebsocketServer", "Server starting...")
                        mWebSocketServer?.start()
                        Log.i("MyWebsocketServer", "Server stopped.")
                    } catch (e: Exception) {
                        Log.e("MyWebsocketServer", "Server crashed", e)
                    }
                }
            }
            // (repeatOnLifecycleが終了したら、サーバーは自動的に停止処理に入る)
        }
    }

    /** LinkPropertiesからIPアドレスを抽出し、Stateを更新する */
    private fun updateIpAddress(linkProperties: LinkProperties) {
        val newIp = linkProperties.linkAddresses
            .firstOrNull { it.address is Inet4Address && it.address.isSiteLocalAddress }
            ?.address?.hostAddress

        if (newIp != null) {
            ipAddressState.value = newIp
            Log.d("NetworkCallback", "Found IP Address: $newIp")
        } else {
            ipAddressState.value = "Not Found (Wi-Fi or Hotspot only)"
            Log.d("NetworkCallback", "IP not found in LinkProperties")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ネットワーク監視を解除
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(networkCallback)

        // サーバーを終了
        thread {
            mWebSocketServer?.stop()
            mWebSocketServer = null
        }
    }
}

// 画面表示用のComposable関数
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerHomeScreen(ipAddress: String) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = { Text(text = "接続中ユーザー一覧") } // タイトル変更
            )
        }
    ) { paddingValues ->
        // ipAddressStateの値が変わると、この画面も自動で再描画される
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffoldからのpaddingを適用
                .padding(all = 8.dp)    // コンテンツ自体のpadding
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectionUserCard(ipAddress)
        }
    }
}

@Composable
fun ConnectionUserCard(text: String) {
    Card(
        modifier = Modifier.padding(vertical = 2.dp).fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "IPアドレス：",
                    fontWeight = FontWeight.Bold
                )
                Text(text = text)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PORT：",
                    fontWeight = FontWeight.Bold
                )
                // ポート番号を 8080 に変更
                Text(text = "8080")
            }
        }
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun ServerHomeScreenPreview() {
    ServerHomeScreen("000.000.000.0")
}

@Composable
fun ServerStartScreen(
    onStartup: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "チャットアプリ",
                fontSize = 36.sp,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "サーバー",
                fontSize = 36.sp,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(48.dp))
            ExtendedFloatingActionButton(
                onClick = {
                    onStartup()
                },
                modifier = Modifier.padding(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "はじめる",
                    fontSize = 22.sp,
                    modifier = Modifier.padding(
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
fun ServerStartScreenPreview() {
    ServerStartScreen(
        onStartup = {}
    )
}

@Composable
fun StopServerConfirmAlert() {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = ""
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "サーバーを終了します。"
                )
                Text(
                    text = "よろしいですか？"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {}
            ) {
                Text(
                    text = "OK",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {}
            ) {
                Text(
                    text = "戻る"
                )
            }
        },
        onDismissRequest = {}
    )
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun StopServerConfirmAlertPreview() {
    StopServerConfirmAlert()
}

@Composable
fun ChatAppServerNavigation(text: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.START.route
    ) {
        composable(route = NavRoutes.START.route) {
            ServerStartScreen(
                onStartup = {
                    navController.navigate(NavRoutes.HOME.route)
                }
            )
        }

        composable(route = NavRoutes.HOME.route) {
            ServerHomeScreen(text)
        }
    }
}