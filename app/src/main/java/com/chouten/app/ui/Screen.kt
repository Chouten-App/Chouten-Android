package com.chouten.app.ui

import com.chouten.app.data.NavigationItems

sealed class Screen(val route: String) {
    object HomePage : Screen(NavigationItems.HomePage.route)
    object SettingsPage : Screen(NavigationItems.SettingsPage.route)
}
