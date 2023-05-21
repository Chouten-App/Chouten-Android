package com.chouten.app.data

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

data class SnackbarAction(
    val actionText: String? = null,
    val action: () -> Unit
)

class SnackbarVisualsWithError(
    override val message: String,
    val isError: Boolean,
    val shouldShowButton: Boolean = false,
    val buttonText: String? = null,
    val customButton: SnackbarAction? = null
) : SnackbarVisuals {
    override val actionLabel: String
        get() = customButton?.actionText ?: if (isError) "Error" else "OK"
    override val withDismissAction: Boolean
        get() = false
    override val duration: SnackbarDuration
        get() = if (!shouldShowButton) SnackbarDuration.Indefinite else SnackbarDuration.Short
}