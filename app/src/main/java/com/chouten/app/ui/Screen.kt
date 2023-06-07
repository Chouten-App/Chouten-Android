package com.chouten.app.ui

import com.chouten.app.data.NavigationItems

sealed class Screen(val route: String) {
    object HomePage : Screen(NavigationItems.HomePage.route)
    object SearchPage : Screen(NavigationItems.SearchPage.route)
    object PlayGroundPage : Screen(NavigationItems.PlayGroundPage.route)
    object MorePage : Screen(NavigationItems.MorePage.route)
    object AppearancePage : Screen(NavigationItems.AppearancePage.route)
    object NetworkPage : Screen(NavigationItems.NetworkPage.route)
    object LogPage : Screen(NavigationItems.LogPage.route)
}
