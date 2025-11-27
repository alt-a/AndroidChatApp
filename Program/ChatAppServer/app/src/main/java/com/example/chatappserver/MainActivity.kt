package com.example.chatappserver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chatappserver.data.ipaddress.MonitorManagerFactory
import com.example.chatappserver.navigation.ChatAppServerNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IPアドレス監視用
        val app = application as MyApplication
        val factory = MonitorManagerFactory(app.ipAddressMonitor)

        setContent {
            // YourAppTheme { ... } のようなテーマで囲うのが一般的です
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // 起動時画面を表示
                ChatAppServerNavigation(factory)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}