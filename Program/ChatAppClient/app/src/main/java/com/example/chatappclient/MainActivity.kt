package com.example.chatappclient // パッケージ名はご自身のものに合わせてください

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel // ★ViewModelをComposeで使うために import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatappclient.navigation.ChatAppClientNavigation
import com.example.chatappclient.navigation.NavRoutes
import com.example.chatappclient.ui.screen.chat.ClientChatScreen
import com.example.chatappclient.ui.screen.login.ClientLoginScreen
import com.example.chatappclient.ui.screen.start.ClientStartScreen
import com.example.chatappclient.ui.theme.ChatAppClientTheme // テーマ名はご自身のものに

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

// --- 仮置きだったプレビュー関数は削除 ---