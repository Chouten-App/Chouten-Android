package com.chouten.app.ui.views.settingsPage.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chouten.app.R
import com.chouten.app.data.AppThemeType
import com.chouten.app.data.Preferences
import com.chouten.app.preferenceHandler
import com.chouten.app.ui.components.SettingsChoice
import com.chouten.app.ui.components.SettingsItem
import com.chouten.app.ui.components.SettingsToggle
import com.chouten.app.ui.views.settingsPage.json
import kotlinx.serialization.encodeToString
import kotlin.reflect.full.declaredMemberProperties

@Composable
fun AppearancePage() {
    val context = LocalContext.current
    val themes =
        Pair(dynamicLightColorScheme(context), dynamicDarkColorScheme(context))
    val surfaceContainer =
        MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val exportJson = rememberSaveable {
        val getColours: ((ColorScheme) -> Map<String, String>) = { theme ->
            theme::class.declaredMemberProperties.associate { member ->
                val hexColor =
                    Integer.toHexString((member.getter.call(theme) as Color).toArgb())
                        .drop(2)
                if (member.name.startsWith("on")) {
                    member.name to "#$hexColor"
                } else {
                    member.name.replaceFirstChar { it.uppercase() } to "#$hexColor"
                }
            }
        }

        val lightPairs = getColours(themes.first).toMutableMap().apply {
            this["SurfaceContainer"] =
                Integer.toHexString(surfaceContainer.toArgb()).drop(2)
        }
        val darkPairs = getColours(themes.second).toMutableMap().apply {
            this["SurfaceContainer"] =
                Integer.toHexString(surfaceContainer.toArgb()).drop(2)
        }

        json.encodeToString(mapOf("light" to lightPairs, "dark" to darkPairs))
    }

    Column {
        SettingsToggle(preference = Preferences.Settings.dynamicColor,
            defaultValue = preferenceHandler.getBoolean(
                Preferences.Settings.dynamicColor.preference.first, true
            ),
            onCheckedChange = { toggle ->
                preferenceHandler.isDynamicColor = toggle
            })
        SettingsItem(modifier = Modifier.clickable {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(
                "Material You Theme", exportJson
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
        SettingsChoice(preference = Preferences.Settings.themeType,
            onPreferenceChange = { updated ->
                preferenceHandler.themeType = updated
            },
            defaultValue = preferenceHandler.getEnum(
                Preferences.Settings.themeType.preference.first,
                AppThemeType.SYSTEM
            ),
            onPreviewSelectionChange = { theme ->
                preferenceHandler.themeType = theme
            })
    }
}