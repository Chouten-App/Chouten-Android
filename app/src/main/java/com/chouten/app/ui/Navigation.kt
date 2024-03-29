package com.chouten.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.navigation.navigation
import com.chouten.app.LogLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.NavigationItems
import com.chouten.app.data.WebviewHandler
import com.chouten.app.ui.components.Subpage
import com.chouten.app.ui.views.home.HomePage
import com.chouten.app.ui.views.home.HomePageViewModel
import com.chouten.app.ui.views.info.InfoPage
import com.chouten.app.ui.views.info.InfoPageViewModel
import com.chouten.app.ui.views.search.SearchPage
import com.chouten.app.ui.views.search.SearchPageViewModel
import com.chouten.app.ui.views.settings.MorePage
import com.chouten.app.ui.views.settings.screens.LogPage
import com.chouten.app.ui.views.settings.screens.NetworkPage
import com.chouten.app.ui.views.settings.screens.appearance.AppearancePage
import com.chouten.app.ui.views.watch.WatchPage
import com.chouten.app.ui.views.watch.WatchPageViewModel

object ViewModels {
    var searchVm: SearchPageViewModel? = null
    var infoVm: InfoPageViewModel? = null
    var watchVm: WatchPageViewModel? = null
    var homeVm: HomePageViewModel? = null
}

@Composable
fun Navigation(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavHost(
        navController = navController,
        startDestination = Screen.HomePage.route
    ) {
        PrimaryDataLayer.isNavigationShown = when (currentRoute) {
            "info/{title}/{url}",
            "watch/{title}/{name}/{url}" -> false

            else -> true
        }

        val searchVm =
            ViewModels.searchVm
        val infoVm = ViewModels.infoVm
        val homeVm = ViewModels.homeVm

        composable(
            route = Screen.HomePage.route,
        ) {
            if (homeVm == null) {
                ViewModels.homeVm =
                    HomePageViewModel(navController.context, WebviewHandler())
            }
            ViewModels.homeVm?.let {
                HomePage(navController, it)
            }
        }
        composable(
            route = Screen.SearchPage.route
        ) {
            if (searchVm == null) {
                ViewModels.searchVm =
                    SearchPageViewModel(navController.context, WebviewHandler())
            }
            ViewModels.searchVm?.let {
                SearchPage(navController, it)
            }

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

            // If the InfoPageViewModel is for a diff`erent title, we create a new one
            if (infoVm?.getUrl() != url) {
                println("The urls are different. The old one is ${infoVm?.getUrl()} and the new one is $url")
                ViewModels.infoVm =
                    InfoPageViewModel(navController.context, url, title)
            }

            infoVm?.let { viewModel ->
                InfoPage(viewModel, navController)
            }
        }

        composable(
            route = "watch/{title}/{name}/{url}",
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                },
                navArgument("name") {
                    type = NavType.StringType
                },
                navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) {
            val title = it.arguments?.getString("title") ?: ""
            val name = it.arguments?.getString("name") ?: ""
            val url = it.arguments?.getString("url") ?: ""

            if (ViewModels.watchVm?.url != url) {
                ViewModels.watchVm =
                    WatchPageViewModel(navController.context, title, name, url)
            }

            ViewModels.watchVm?.let { viewModel ->
                WatchPage(viewModel)
            }
        }

        navigation(
            route = Screen.MorePage.route,
            startDestination = "more/default"
        ) {
            composable(
                route = "more/default"
            ) {
                MorePage(
                    navController
                )
            }

            composable(
                route = Screen.AppearancePage.route
            ) {
                Subpage(
                    title = stringResource(NavigationItems.AppearancePage.name),
                    navController = navController
                ) {
                    AppearancePage()
                }
            }

            composable(
                route = Screen.NetworkPage.route
            ) {
                Subpage(
                    title = stringResource(NavigationItems.NetworkPage.name),
                    navController = navController
                ) {
                    NetworkPage()
                }
            }

            composable(
                route = Screen.LogPage.route
            ) {
                Subpage(
                    title = stringResource(NavigationItems.LogPage.name),
                    navController = navController
                ) {
                    LogPage(provider = LogLayer)
                }
            }
        }
    }
}

data class BottomNavItem(
    @StringRes val name: Int,
    val route: String,
    val activeIcon: ImageVector? = null,
    val inactiveIcon: ImageVector? = activeIcon,
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
        items.iterator().forEach { (name, route, activeIcon, inactiveIcon, badgeCount) ->
            // If the item is a subroute of the current route, we consider it selected
            val selected =
                backStackEntry?.destination?.route?.startsWith(route) == true
            val icon: @Composable () -> Unit = {
                Icon(
                    // Note: If the icon is not provided, the app will crash
                    imageVector = if (selected) activeIcon!! else inactiveIcon!!,
                    contentDescription = stringResource(name),
                    tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            NavigationBarItem(selected = selected,
                alwaysShowLabel = true,
                onClick = {
                    onItemClick(
                        BottomNavItem(
                            name,
                            route,
                            activeIcon,
                            inactiveIcon,
                            badgeCount
                        )
                    )
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (badgeCount > 0) {
                            BadgedBox(badge = {
                                Badge(
                                    modifier = Modifier.offset((-2).dp, 2.dp),
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    val count =
                                        if (badgeCount > 99) "99+" else badgeCount.toString()
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
                        text = stringResource(name),
                        textAlign = TextAlign.Center,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}