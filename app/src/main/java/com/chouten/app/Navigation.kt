package com.chouten.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chouten.app.ui.views.homePage.HomePage
import com.chouten.app.ui.views.settingsPage.SettingsPage

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.HomePage.route) {
        composable(Screen.HomePage.route) {
            HomePage(context = navController.context, navController = navController)
        }
        composable(Screen.SettingsPage.route) {
            SettingsPage()
        }
    }
}