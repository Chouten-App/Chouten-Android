package com.chouten.app.ui.views.settingsPage

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.chouten.app.R
import com.chouten.app.data.ChoutenSetting
import com.chouten.app.data.Preferences
import kotlin.reflect.full.declaredMemberProperties

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsPage(provider: SettingsPageViewModel = SettingsPageViewModel()) {
    val context = LocalContext.current
    val themes =
        Pair(dynamicLightColorScheme(context), dynamicDarkColorScheme(context))
    val exportJson = remember {
        val getColours: ((ColorScheme) -> String) = { theme ->
            theme::class.declaredMemberProperties.joinToString(",\n") { member ->
                val hexColor =
                    Integer.toHexString((member.getter.call(theme) as Color).toArgb())
                        .drop(2)
                "\t\t\"${member.name}\": \"#$hexColor\""
            }
        }

        val lightPairs = getColours(themes.first)
        val darkPairs = getColours(themes.second)
        "{\n\t\"light\": {\n$lightPairs\n\t},\n\t\"dark\": {\n$darkPairs\n\t}\n}"
    }

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
            SettingsItem(modifier = Modifier
                .clickable {
                    val clipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData =
                        ClipData.newPlainText(
                            "Material You Theme",
                            exportJson
                        )
                    clipboardManager.setPrimaryClip(clipData)
                },
                { },
                { Text(text = stringResource(R.string.export_theme__title)) },
                { Text(text = stringResource(R.string.export_theme__desc)) }) {
                Icon(
                    Icons.Default.CopyAll,
                    stringResource(R.string.export_theme__desc)
                )
            }
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
        preference.onToggle?.invoke()
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