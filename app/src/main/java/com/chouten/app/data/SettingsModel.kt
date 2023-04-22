package com.chouten.app.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class ChoutenSetting(
    @StringRes val text: Int,
    @StringRes val secondaryText: Int? = null,
    val icon: ImageVector? = null,
    val preference: Pair<String, Any>,
    val constraints: (() -> Boolean)? = null,
    val onToggle: ((Boolean) -> Unit)? = null
)
