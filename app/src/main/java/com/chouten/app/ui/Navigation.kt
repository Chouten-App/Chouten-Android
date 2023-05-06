package com.chouten.app.ui

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.chouten.app.data.WebviewHandler
import com.chouten.app.ui.views.homePage.HomePage
import com.chouten.app.ui.views.infoPage.InfoPage
import com.chouten.app.ui.views.infoPage.InfoPageViewModel
import com.chouten.app.ui.views.searchPage.SearchPage
import com.chouten.app.ui.views.searchPage.SearchPageViewModel
import com.chouten.app.ui.views.settingsPage.SettingsPage


@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomePage.route
    ) {
        val searchPageViewModel =
            SearchPageViewModel(navController.context, WebviewHandler())
        var infoVm: InfoPageViewModel? = null

        composable(
            route = Screen.HomePage.route,
        ) {
            HomePage(navController.context)
            infoVm = null
        }
        composable(
            route = Screen.SearchPage.route
        ) {
            SearchPage(navController, searchPageViewModel)
            infoVm = null
        }
        composable(
            // The route will be dynamic, with params:
            // - title: String
            // - url: String
            route = "info/{title}/{url}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) {
            // We want to pass the title to the InfoPageViewModel
            // so that it can load the correct page.

            val title = it.arguments?.getString("title") ?: ""
            val url = it.arguments?.getString("url") ?: ""

            if (infoVm == null)
            {
                infoVm = InfoPageViewModel(navController.context, url, title)
            }

            infoVm?.let { viewModel ->
                InfoPage(viewModel)
            }
        }
        composable(
            route = Screen.SettingsPage.route,
        ) {
            SettingsPage()
            infoVm = null
        }
    }
}

data class BottomNavItem(
    @StringRes val name: Int,
    val route: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector = activeIcon,
    val badgeCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>,
    onItemClick: (BottomNavItem) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    NavigationBar {
        items.forEach { item ->
            val selected = backStackEntry?.destination?.route == item.route
            val icon: @Composable () -> Unit = {
                Icon(
                    imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                    contentDescription = stringResource(item.name),
                    tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            NavigationBarItem(selected = selected,
                alwaysShowLabel = true,
                onClick = { onItemClick(item) },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (item.badgeCount > 0) {
                            BadgedBox(badge = {
                                Badge(
                                    modifier = Modifier.offset((-2).dp, 2.dp),
                                    containerColor = MaterialTheme.colorScheme.error) {
                                    val count =
                                        if (item.badgeCount > 99) "99+" else item.badgeCount.toString()
                                    Text(
                                        text = count,
                                        color = MaterialTheme.colorScheme.onError,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.semantics {
                                            contentDescription =
                                                "$count new notifications"
                                        }
                                    )
                                }
                            }) {
                                icon()
                            }
                        } else {
                            icon()
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.name),
                        textAlign = TextAlign.Center,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                })
        }
    }
}