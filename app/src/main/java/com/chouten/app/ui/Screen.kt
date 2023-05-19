package com.chouten.app.ui

import com.chouten.app.data.NavigationItems

sealed class Screen(val route: String) {
    object HomePage : Screen(NavigationItems.HomePage.route)
    object SearchPage : Screen(NavigationItems.SearchPage.route)
    object MorePage : Screen(NavigationItems.MorePage.route)
    object LogPage : Screen(NavigationItems.LogPage.route)
    object AppearancePage : Screen(NavigationItems.AppearancePage.route)
}
