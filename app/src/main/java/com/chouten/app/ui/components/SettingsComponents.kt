package com.chouten.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chouten.app.R
import com.chouten.app.data.ChoutenSetting

@Composable
fun SettingsToggle(
    preference: ChoutenSetting,
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit),
    defaultValue: Boolean = false
) {
    var toggleState by rememberSaveable { mutableStateOf(defaultValue) }
    SettingsItem(modifier = modifier, // modifier.clickable { makes it buggy
        { preference.icon?.let { Icon(it, stringResource(preference.text)) } },
        { Text(stringResource(preference.text)) },
        { preference.secondaryText?.let { Text(stringResource(it)) } }) {
        Switch(
            enabled = preference.constraints?.let { it() } != false,
            checked = toggleState,
            onCheckedChange = {
                preference.onToggle?.invoke(toggleState)
                onCheckedChange.invoke(it)
                toggleState = it
            })
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
    var isOpen by rememberSaveable {
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
            onPreviewSelection = { onPreviewSelectionChange(it) })
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
    var selected by rememberSaveable { mutableStateOf(defaultValue) }
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
                            }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = e == selected, onClick = {
                            selected = e
                            onPreviewSelection(e)
                        })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            e.toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
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

// This one does not use enums
// as they cannot be modified at runtime
@Composable
fun <T> SettingsListChoice(
    preference: ChoutenSetting,
    modifier: Modifier = Modifier,
    possibleValues: List<T>,
    defaultValue: T,
    title: @Composable () -> Unit,
    onClose: () -> Unit,
    onSelection: (T) -> Unit,
    onPreviewSelection: (T) -> Unit = {},
) {
    var isOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var selected by rememberSaveable {
        mutableStateOf(defaultValue)
    }

    SettingsItem(modifier.clickable {
        isOpen = true
    },
        { preference.icon?.let { Icon(it, stringResource(preference.text)) } },
        { Text(stringResource(preference.text)) },
        { preference.secondaryText?.let { Text(stringResource(it)) } }) {
        AnimatedVisibility(visible = isOpen) {
            AlertDialog(onDismissRequest = {
                isOpen = false
            }, title = title, text = {
                Column {
                    possibleValues.forEach { v ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = v
                                    onPreviewSelection(v)
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = v == selected, onClick = {
                                selected = v
                                onPreviewSelection(v)
                            })
                            Spacer(Modifier.width(8.dp))
                            Text(
                                possibleValues.find { it == v }.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }, confirmButton = {
                TextButton(onClick = { onClose(); onSelection(selected); isOpen = false }) {
                    Text(stringResource(R.string.confirm))
                }
            })
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Subpage(
    title: String,
    navController: NavController,
    content: @Composable () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Text(title)
            }
        })
    }) { scaffoldPadding ->
        Box(
            Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}