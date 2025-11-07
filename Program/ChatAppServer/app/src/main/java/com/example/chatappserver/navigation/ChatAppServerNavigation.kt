package com.example.chatappserver.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatappserver.ui.screen.home.ServerHomeScreen
import com.example.chatappserver.ui.screen.start.ServerStartScreen

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
            ServerHomeScreen(
                ipAddress = text,
                onStop = {
                    navController.popBackStack()
                }
            )
        }
    }
}