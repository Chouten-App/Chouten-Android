package com.chouten.app.ui.views.settingsPage

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chouten.app.R
import com.chouten.app.data.AppThemeType
import com.chouten.app.data.ChoutenSetting
import com.chouten.app.data.Preferences
import com.chouten.app.preferenceHandler
import kotlin.reflect.full.declaredMemberProperties

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsPage() {
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
                preference = Preferences.Settings.dynamicColor,
                defaultValue = preferenceHandler.getBoolean(
                    Preferences.Settings.dynamicColor.preference.first,
                    true
                ),
                onCheckedChange = { toggle ->
                    preferenceHandler.isDynamicColor = toggle
                }
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
            SettingsChoice(
                preference = Preferences.Settings.themeType,
                onPreferenceChange = { updated ->
                    preferenceHandler.themeType = updated
                },
                defaultValue = preferenceHandler.getEnum(
                    Preferences.Settings.themeType.preference.first,
                    AppThemeType.SYSTEM
                ),
                onPreviewSelectionChange = { theme ->
                    preferenceHandler.themeType = theme
                }
            )
        }
    }
}

@Composable
fun SettingsToggle(
    preference: ChoutenSetting,
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit),
    defaultValue: Boolean = false
) {
    var toggleState by remember { mutableStateOf(defaultValue) }
    SettingsItem(modifier.clickable {
        preference.onToggle?.invoke(toggleState)
        onCheckedChange.invoke(toggleState)
        toggleState = !toggleState
    },
        { preference.icon?.let { Icon(it, stringResource(preference.text)) } },
        { Text(stringResource(preference.text)) },
        { preference.secondaryText?.let { Text(stringResource(it)) } }) {
        Switch(
            enabled = preference.constraints?.let { it() } ?: true,
            checked = toggleState,
            onCheckedChange = {
                preference.onToggle?.invoke(toggleState)
                onCheckedChange.invoke(it)
                toggleState = !toggleState
            }
        )
    }
}

@Composable
inline fun <reified T : Enum<T>> SettingsChoice(
    preference: ChoutenSetting,
    modifier: Modifier = Modifier,
    crossinline onPreferenceChange: (T) -> Unit,
    crossinline onPreviewSelectionChange: (T) -> Unit = {},
    defaultValue: T
) {
    var isOpen by remember {
        mutableStateOf(false)
    }

    SettingsItem(modifier.clickable {
        isOpen = true
    },
        { preference.icon?.let { Icon(it, stringResource(preference.text)) } },
        { Text(stringResource(preference.text)) },
        { preference.secondaryText?.let { Text(stringResource(it)) } }) {
        SettingsChoicePopup(visible = isOpen,
            title = { Text(text = stringResource(preference.text)) },
            defaultValue = defaultValue,
            onClose = { isOpen = false },
            onSelection = { onPreferenceChange(it); isOpen = false },
            onPreviewSelection = { onPreviewSelectionChange(it) }
        )
    }
}

@Composable
inline fun <reified T : Enum<T>> SettingsChoicePopup(
    visible: Boolean,
    noinline title: @Composable () -> Unit,
    defaultValue: T,
    noinline onClose: () -> Unit,
    noinline onSelection: (T) -> Unit,
    noinline onPreviewSelection: (T) -> Unit = {},
) {
    var selected by remember { mutableStateOf(defaultValue) }
    AnimatedVisibility(visible = visible) {
        AlertDialog(onDismissRequest = {
            onClose()
            if (selected != defaultValue) {
                selected = defaultValue
                onSelection(defaultValue)
            }
        }, title = title, text = {
            Column {
                enumValues<T>().forEach { e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = e
                                onPreviewSelection(e)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = e == selected, onClick = {
                                selected = e
                                onPreviewSelection(e)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(e.toString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = { onClose(); onSelection(selected) }) {
                Text(stringResource(R.string.confirm))
            }
        })
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