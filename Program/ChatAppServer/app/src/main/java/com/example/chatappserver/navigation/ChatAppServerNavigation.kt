package com.example.chatappserver.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatappserver.ui.screen.home.ServerHomeScreen
import com.example.chatappserver.ui.screen.start.ServerStartScreen

/**
 * 画面遷移用 ナビゲーショングラフ
 */
@Composable
fun ChatAppServerNavigation(text: String) {
    // ナビゲーション状態管理コントローラ
    val navController = rememberNavController()

    // 画面遷移ホスト
    NavHost(
        navController = navController,
        startDestination = NavRoutes.START.route
    ) {
        // ----- ルート定義 -----
        // 起動時画面
        composable(route = NavRoutes.START.route) {
            ServerStartScreen(
                onStartup = {
                    // ホーム画面へ遷移
                    navController.navigate(NavRoutes.HOME.route)
                }
            )
        }

        // ホーム画面
        composable(route = NavRoutes.HOME.route) {
            ServerHomeScreen(
                ipAddress = text,
                onStop = {
                    // 起動時画面に戻る
                    navController.popBackStack()
                }
            )
        }
    }
}