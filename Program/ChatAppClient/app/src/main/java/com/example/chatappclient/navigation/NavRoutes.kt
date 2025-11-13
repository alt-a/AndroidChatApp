package com.example.chatappclient.navigation

/**
 * 画面遷移用 ナビゲーションルート定義
 */
enum class NavRoutes(val route: String) {
    START("start"), // スタート画面
    LOGIN("login"), // ユーザー名入力画面
    CHAT("chat")    // チャット画面
}