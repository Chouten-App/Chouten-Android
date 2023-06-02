package com.chouten.app.ui.views.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chouten.app.R
import com.chouten.app.data.Preferences
import com.chouten.app.preferenceHandler
import com.chouten.app.ui.Screen
import com.chouten.app.ui.components.SettingsItem
import com.chouten.app.ui.components.SettingsToggle
import kotlinx.serialization.json.Json

val json = Json { prettyPrint = true }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorePage(
    navHost: NavController
) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Text(text = stringResource(R.string.navbar_more_header),
                style = MaterialTheme.typography.headlineMedium)
        }, modifier = Modifier.fillMaxWidth()
        )
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            SettingsToggle(
                preference = Preferences.Settings.downloadedOnly,
                defaultValue = preferenceHandler.getBoolean(
                    Preferences.Settings.downloadedOnly.preference.first,
                    false
                ),
                onCheckedChange = { toggle ->
                    preferenceHandler.isOfflineMode = toggle
                }
            )
            SettingsToggle(
                preference = Preferences.Settings.incognito,
                defaultValue = preferenceHandler.getBoolean(
                    Preferences.Settings.incognito.preference.first,
                    false
                ),
                onCheckedChange = { toggle ->
                    preferenceHandler.isIncognito = toggle
                }
            )
            Divider(Modifier.padding(16.dp))
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