package com.example.chatappclient.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel   // ★ViewModelをComposeで使うために import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatappclient.data.websocket.MyWebsocketClient
import com.example.chatappclient.ui.screen.chat.ClientChatScreen
import com.example.chatappclient.ui.screen.login.ClientLoginScreen
import com.example.chatappclient.ui.screen.start.ClientStartScreen

/**
 * アプリの画面遷移（ナビゲーション）を管理する Composable
 */
@Composable
fun ChatAppClientNavigation() {
    // ナビゲーション状態管理コントローラ
    val navController = rememberNavController()

    // ★ViewModelのインスタンスを作成★
    // viewModel() 関数が、Activityが生きている間ずっと
    // 同一の MyWebsocketClient インスタンスを保持してくれます。
    val viewModel: MyWebsocketClient = viewModel()

    // 画面遷移ホスト
    NavHost(
        navController = navController,
        startDestination = NavRoutes.START.route    // 最初に表示する画面
    ) {

        // ----- ルート定義 -----
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
                viewModel = viewModel, // ★ViewModelを渡す
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
                viewModel = viewModel, // ★ログイン画面と【同じ】ViewModelを渡す
                // ★「切断」時にログイン画面に戻るコールバックを渡す
                onDisconnect = {
                    // 起動時画面に戻る (スタックをクリア)
                    navController.popBackStack(route = NavRoutes.START.route, inclusive = false)
                }
            )
        }
    }
}