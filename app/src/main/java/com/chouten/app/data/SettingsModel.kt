package com.chouten.app.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.chouten.app.ui.views.settingsPage.SettingType

data class ChoutenSetting(
    @StringRes val text: Int,
    @StringRes val secondaryText: Int? = null,
    val icon: ImageVector? = null,
    val preference: Pair<String, Any>,
    val settingType: SettingType? = SettingType.TOGGLE,
    val constraints: (() -> Boolean)? = null,
    val onToggle: (() -> Unit)? = null
)
