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

/**
 * アプリの画面遷移（ナビゲーション）を管理する Composable
 */
@Composable
fun ChatAppClientNavigation() {
    // 1. ナビゲーションの状態を管理する「コントローラー」を作成
    val navController = rememberNavController()

    // 2. ★ViewModelのインスタンスを作成★
    // viewModel() 関数が、Activityが生きている間ずっと
    // 同一の ChatViewModel インスタンスを保持してくれます。
    val chatViewModel: ChatViewModel = viewModel()

    // 3. 画面遷移のホスト (NavHost) を設定
    NavHost(
        navController = navController,
        startDestination = NavRoutes.START.route    // 最初に表示する画面
    ) {

        // 4. 各画面のルートを定義 (中身を本物に入れ替え)

        // 起動時画面
        composable(route = NavRoutes.START.route) {
            ClientStartScreen(
                onStartup = {
                    // ユーザー名入力画面へ遷移
                    navController.navigate(NavRoutes.LOGIN.route)
                }
            )
        }

        // ログイン画面
        composable(route = NavRoutes.LOGIN.route) {
            ClientLoginScreen(
                viewModel = chatViewModel, // ★ViewModelを渡す
                onConnect = {
                    // 接続ボタンが押されたら、チャット画面に遷移
                    navController.navigate("chat")
                }
            )
        }

        // 画面2: チャット画面
        composable(route = "chat") {
            ClientChatScreen(
                viewModel = chatViewModel, // ★ログイン画面と【同じ】ViewModelを渡す
                // ★「切断」時にログイン画面に戻るコールバックを渡す
                onDisconnect = {
                    // "login" 画面に戻る (スタックをクリア)
                    navController.popBackStack(route = "login", inclusive = false)
                }
            )
        }
    }
}

// --- 仮置きだったプレビュー関数は削除 ---