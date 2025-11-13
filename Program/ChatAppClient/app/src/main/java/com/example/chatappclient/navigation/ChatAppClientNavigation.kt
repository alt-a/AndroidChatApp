package com.example.chatappclient.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatappclient.ChatViewModel
import com.example.chatappclient.ui.screen.chat.ClientChatScreen
import com.example.chatappclient.ui.screen.login.ClientLoginScreen
import com.example.chatappclient.ui.screen.start.ClientStartScreen

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

        // ユーザー名入力画面
        composable(route = NavRoutes.LOGIN.route) {
            ClientLoginScreen(
                viewModel = chatViewModel, // ★ViewModelを渡す
                onConnect = {
                    // 接続ボタンが押されたら、チャット画面に遷移
                    navController.navigate(NavRoutes.CHAT.route)
                },
                onBack = {
                    // 起動時画面に戻る
                    navController.popBackStack()
                }
            )
        }

        // チャット画面
        composable(route = NavRoutes.CHAT.route) {
            ClientChatScreen(
                viewModel = chatViewModel, // ★ログイン画面と【同じ】ViewModelを渡す
                // ★「切断」時にログイン画面に戻るコールバックを渡す
                onDisconnect = {
                    // 起動時画面に戻る (スタックをクリア)
                    navController.popBackStack(route = NavRoutes.START.route, inclusive = false)
                }
            )
        }
    }
}