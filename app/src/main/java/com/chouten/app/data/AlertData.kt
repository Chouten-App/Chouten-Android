package com.chouten.app.data

import androidx.compose.ui.graphics.vector.ImageVector

data class AlertData(
    val title: String = "Notice",
    val message: String,
    val confirmButtonText: String? = null,
    val confirmButtonAction: (() -> Unit)? = null,
    val cancelButtonText: String? = null,
    val cancelButtonAction: (() -> Unit)? = null,
    val icon: ImageVector? = null,
)