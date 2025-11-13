package com.example.chatappclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.chatappclient.navigation.ChatAppClientNavigation
import com.example.chatappclient.ui.theme.ChatAppClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 描画領域を画面全体に広げる
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ChatAppClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ★アプリ本体の画面(NavHost)を呼び出す
                    ChatAppClientNavigation()
                }
            }
        }
    }
}