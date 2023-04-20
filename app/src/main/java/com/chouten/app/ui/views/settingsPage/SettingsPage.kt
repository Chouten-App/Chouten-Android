package com.chouten.app.ui.views.settingsPage

import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.chouten.app.data.ChoutenSetting
import com.chouten.app.data.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsPage(provider: SettingsPageViewModel = SettingsPageViewModel()) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text("Settings")
        })
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            SettingsToggle(
                provider, preference = Preferences.Settings.dynamicColor
            )
        }
    }
}

@Composable
fun SettingsToggle(
    provider: SettingsPageViewModel,
    modifier: Modifier = Modifier,
    preference: ChoutenSetting,
    onCheckedChange: ((Boolean) -> Unit)? = { provider.toggleSetting(preference.preference.first) }
) {
    SettingsItem(modifier.clickable {
        provider.toggleSetting(
            preference.preference.first, SettingType.TOGGLE
        )
    },
        { preference.icon?.let { Icon(it, stringResource(preference.text)) } },
        { Text(stringResource(preference.text)) },
        { preference.secondaryText?.let { Text(stringResource(it)) } }) {
        Switch(
            enabled = preference.constraints?.let { it() } ?: true,
            checked = provider.settings[preference.preference.first] as? Boolean
                ?: false,
            onCheckedChange = {
                preference.onToggle?.invoke()
                onCheckedChange?.invoke(it)
            }
        )
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    text: @Composable () -> Unit,
    secondaryText: @Composable (() -> Unit) = { },
    trailing: @Composable (() -> Unit) = { },
) {
    ListItem(
        modifier = modifier,
        leadingContent = icon,
        headlineContent = text,
        supportingContent = secondaryText,
        trailingContent = trailing,
    )
}