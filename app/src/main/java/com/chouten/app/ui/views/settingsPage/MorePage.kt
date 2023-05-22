package com.chouten.app.ui.views.settingsPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.chouten.app.R
import com.chouten.app.ui.Screen
import com.chouten.app.ui.components.SettingsItem
import kotlinx.serialization.json.Json

val json = Json { prettyPrint = true }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorePage(
    navHost: NavController
) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(stringResource(R.string.navbar_more))
        })
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            SettingsItem(
                modifier = Modifier.clickable {
                    navHost.navigate(Screen.AppearancePage.route)
                },
                icon = {},
                text = { Text(stringResource(R.string.appearance__title)) },
                secondaryText = { Text(stringResource(R.string.appearance__desc)) },
                trailing = {
                    Icon(
                        Icons.Default.ChevronRight,
                        stringResource(R.string.log__title)
                    )
                },
            )
            SettingsItem(
                modifier = Modifier.clickable {
                    navHost.navigate(Screen.NetworkPage.route)
                },
                icon = {},
                text = { Text(stringResource(R.string.network__title)) },
                secondaryText = { Text(stringResource(R.string.network__desc)) },
                trailing = {
                    Icon(
                        Icons.Default.ChevronRight,
                        stringResource(R.string.network__title)
                    )
                },
            )
            SettingsItem(
                modifier = Modifier.clickable {
                    navHost.navigate(Screen.LogPage.route)
                },
                icon = {},
                text = { Text(stringResource(R.string.log__title)) },
                secondaryText = { Text(stringResource(R.string.log__desc)) },
                trailing = {
                    Icon(
                        Icons.Default.ChevronRight,
                        stringResource(R.string.log__title)
                    )
                },
            )
        }
    }
}