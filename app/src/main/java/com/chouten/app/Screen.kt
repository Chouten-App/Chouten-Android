package com.chouten.app

sealed class Screen(val route: String){
    object HomePage : Screen("homePage")
    object SettingsPage : Screen("settingsPage")
}
