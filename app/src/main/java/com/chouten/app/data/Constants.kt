package com.chouten.app.data

import android.os.Build
import android.os.Environment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import com.chouten.app.R
import com.chouten.app.ui.BottomNavItem
import java.io.File

object AppPaths {
    val baseDir =
        Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_DOCUMENTS}/Chouten/")
    val _toCreate = listOf("Modules", "Themes")
    val addedDirs = mutableMapOf<String, File>()
}

object RequestCodes {
    const val allowAllFiles = 1
}

object Preferences {
    const val SelectedModule = "SelectedModule"

    object Settings {
        val dynamicColor = ChoutenSetting(
            R.string.dynamic_colour_toggle__title,
            R.string.dynamic_colour_toggle__desc,
            preference = Pair("dynamicColor", Boolean),
            constraints = { Build.VERSION.SDK_INT >= 31 }  // Disable if not on Android 12+
        )
        val themeType = ChoutenSetting(
            R.string.appearance__title,
            R.string.appearance__desc,
            preference = Pair("themeType", Enum),
        )
        val dns = ChoutenSetting(
            R.string.dns__title,
            R.string.dns__desc,
            preference = Pair("dns", String),
        )
    }
}

object NavigationItems {
    val HomePage = BottomNavItem(
        name = R.string.navbar_home,
        route = "home/",
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home,
    )
    val SearchPage = BottomNavItem(
        name = R.string.navbar_search,
        route = "search/",
        activeIcon = Icons.Filled.Search,
        inactiveIcon = Icons.Outlined.Search,
    )
    val MorePage = BottomNavItem(
        name = R.string.navbar_more,
        route = "more/",
        activeIcon = Icons.Filled.MoreHoriz,
        inactiveIcon = Icons.Outlined.MoreHoriz
    )
    val AppearancePage = BottomNavItem(
        name = R.string.appearance__title,
        route = "more/appearance",
    )
    val NetworkPage = BottomNavItem(
        name = R.string.network__title,
        route = "more/network",
    )
    val LogPage = BottomNavItem(
        name = R.string.settings_submenu_log,
        route = "more/log",
    )
}

enum class AppThemeType(val printable: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System");

    override fun toString(): String {
        return this.printable
    }
}

enum class CustomDNS(val printable: String) {
    NONE("None"),
    CLOUDFLARE("Cloudflare"),
    GOOGLE("Google"),
    ADGUARD("AdGuard");

    override fun toString(): String {
        return this.printable
    }
}