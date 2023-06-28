package com.chouten.app.ui.views.settings.screens.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.R
import com.chouten.app.data.AppThemeType
import com.chouten.app.data.Preferences
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.preferenceHandler
import com.chouten.app.ui.components.SettingsChoice
import com.chouten.app.ui.components.SettingsItem
import com.chouten.app.ui.components.SettingsToggle


@Composable
fun AppearancePage() {
    val context = LocalContext.current
    val vm = AppearanceViewModel(context, MaterialTheme.colorScheme)

    val isNamePromptVisible = rememberSaveable { mutableStateOf(false) }
    val themeName = rememberSaveable { mutableStateOf("") }
    val labelText = rememberSaveable { mutableStateOf(R.string.export_theme__label) }

    val successMessage = stringResource(R.string.export_theme__success)

    val exportTheme = {
        labelText.value = R.string.export_theme__label
        if (vm.exportTheme(themeName.value)) {
            isNamePromptVisible.value = false
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    message = successMessage,
                    isError = false,
                    // TODO: Add custom button
                    // which opens the folder where the theme was exported
                )
            )
        } else {
            labelText.value = R.string.export_theme__error
        }
    }

    val focusManager = LocalFocusManager.current

    Column {

        AnimatedVisibility(visible = isNamePromptVisible.value) {
            // We want an Alert with a text field for the user to enter the name of the theme
            AlertDialog(onDismissRequest = {
                isNamePromptVisible.value = false
            }, title = {
                Text(text = stringResource(R.string.export_theme__title))
            }, text = {
                OutlinedTextField(
                    value = themeName.value,
                    onValueChange = { themeName.value = it },
                    label = {
                        Text(
                            stringResource(R.string.export_theme__label),
                            color = if (labelText.value == R.string.export_theme__error) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        exportTheme()
                        focusManager.clearFocus()
                    })
                )
            }, confirmButton = {
                TextButton(onClick = {
                    exportTheme()
                }) {
                    Text(text = stringResource(R.string.export_theme__title))
                }
            }, dismissButton = {
                TextButton(onClick = {
                    isNamePromptVisible.value = false
                }) {
                    Text(text = stringResource(R.string.cancel))
                }
            })
        }

        SettingsToggle(preference = Preferences.Settings.dynamicColor,
            defaultValue = preferenceHandler.getBoolean(
                Preferences.Settings.dynamicColor.preference.first, true
            ),
            onCheckedChange = { toggle ->
                preferenceHandler.isDynamicColor = toggle
            })
        SettingsToggle(preference = Preferences.Settings.oledTheme,
            defaultValue = preferenceHandler.getBoolean(
                Preferences.Settings.oledTheme.preference.first, false
            ),
            onCheckedChange = { toggle ->
                preferenceHandler.isOledTheme = toggle
            })
        SettingsItem(modifier = Modifier.clickable {
            isNamePromptVisible.value = true
        },
            { },
            { Text(text = stringResource(R.string.export_theme__title)) },
            { Text(text = stringResource(R.string.export_theme__desc)) }) {
            Icon(
                Icons.Default.CopyAll, stringResource(R.string.export_theme__desc)
            )
        }
        SettingsChoice(preference = Preferences.Settings.themeType,
            onPreferenceChange = { updated ->
                preferenceHandler.themeType = updated
            },
            defaultValue = preferenceHandler.getEnum(
                Preferences.Settings.themeType.preference.first, AppThemeType.SYSTEM
            ),
            onPreviewSelectionChange = { theme ->
                preferenceHandler.themeType = theme
            })
    }
}