package com.chouten.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.chouten.app.data.DataLayer
import com.chouten.app.data.LogDataLayer
import com.chouten.app.data.ModuleDataLayer
import com.chouten.app.data.NavigationItems
import com.chouten.app.ui.BottomNavigationBar
import com.chouten.app.ui.Navigation
import com.chouten.app.ui.components.Snackbar

lateinit var ModuleLayer: ModuleDataLayer
lateinit var LogLayer: LogDataLayer
val PrimaryDataLayer = DataLayer()

fun initializeRepositories() {
    ModuleLayer = ModuleDataLayer()
    LogLayer = LogDataLayer()
}

@Composable
fun ChoutenApp() {
    val navController = rememberNavController()
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { }, bottomBar = {
        AnimatedVisibility(
            visible = PrimaryDataLayer.isNavigationShown,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            BottomNavigationBar(navController = navController, items = listOf(
                NavigationItems.HomePage,
                NavigationItems.SearchPage,
                NavigationItems.MorePage,
            ), onItemClick = {
                navController.navigate(route = it.route)
            })
        }
    }, snackbarHost = { Snackbar() }, content = { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Navigation(navController)
        }
    })
}