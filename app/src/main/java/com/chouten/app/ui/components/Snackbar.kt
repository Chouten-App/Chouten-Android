package com.chouten.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.ui.theme.shapes
import kotlinx.coroutines.launch
import androidx.compose.material3.Snackbar as MaterialSnackbar

@Composable
fun Snackbar() {
    val snackbarState = SnackbarHostState()

    // Observe the Flow of the Snackbar Queue
    val owner = LocalLifecycleOwner.current
    PrimaryDataLayer.snackbarQueue.observe(owner) {
        it.forEach { snackbarItem ->
            owner.lifecycleScope.launch {
                snackbarState.showSnackbar(
                    snackbarItem
                )
            }
            PrimaryDataLayer.popSnackbarQueue()
        }
    }

    SnackbarHost(hostState = snackbarState) { data ->
        val extendedVisuals = data.visuals as? SnackbarVisualsWithError
        val isError =
            extendedVisuals?.isError
                ?: false
        val buttonColor = if (isError) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }

        MaterialSnackbar(
            modifier = Modifier.padding(12.dp),
            containerColor = if (!isError) {
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    10.dp
                )
            } else {
                MaterialTheme.colorScheme.error
            },
            contentColor = if (!isError) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onError
            },
            action = {
                TextButton(
                    onClick = {
                        extendedVisuals?.customButton?.action?.invoke()
                            ?: if (isError) data.dismiss() else data.performAction()
                    },
                    shape = shapes.extraSmall,
                    colors = buttonColor
                ) {
                    extendedVisuals?.customButton?.actionText?.let {
                        Text(
                            it
                        )
                    } ?: extendedVisuals?.buttonText?.let {
                        Text(
                            it
                        )
                    } ?: Icon(Icons.Default.Close, "Dismiss")
                }
            }
        ) {
            Text(
                text = data.visuals.message,
                maxLines = 8
            )
        }
    }
}