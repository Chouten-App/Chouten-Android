package com.chouten.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.R
import com.chouten.app.data.AlertData
import kotlinx.coroutines.launch

@Composable
fun ChoutenAlert(alert: AlertData) {

    val isShown = rememberSaveable {
        mutableStateOf(true)
    }

    val defaultDismiss = {
        PrimaryDataLayer.popAlertQueue()
        isShown.value = false
    }

    val owner = LocalLifecycleOwner.current

    AnimatedVisibility(visible = isShown.value) {
        AlertDialog(onDismissRequest = {
            alert.cancelButtonAction?.invoke()
            defaultDismiss()
        }, title = {
            Text(text = alert.title)
        }, text = {
            Text(text = alert.message)
        }, confirmButton = {
            FilledTonalButton(
                onClick = {
                    alert.confirmButtonAction?.invoke()
                    defaultDismiss()
                },
            ) {
                Text(
                    text = alert.confirmButtonText
                        ?: stringResource(id = R.string.ok)
                )
            }
        }, dismissButton = {
            if (alert.cancelButtonText != null && alert.cancelButtonAction != null) {
                TextButton(
                    onClick = {
                        owner.lifecycleScope.launch {
                            alert.cancelButtonAction.invoke()
                            defaultDismiss()
                        }
                    },
                ) {
                    Text(text = alert.cancelButtonText)
                }
            }
        })
    }
}
