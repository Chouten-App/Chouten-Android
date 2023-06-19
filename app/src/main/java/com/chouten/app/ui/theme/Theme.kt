package com.chouten.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import com.chouten.app.data.AppThemeType
import com.chouten.app.preferenceHandler

private val darkColorScheme =
    darkColorScheme(
        primary = md_dark_primary,
        onPrimary = md_dark_onPrimary,
        primaryContainer = md_dark_primaryContainer,
        onPrimaryContainer = md_dark_onPrimaryContainer,
        inversePrimary = md_dark_inversePrimary,
        secondary = md_dark_secondary,
        onSecondary = md_dark_onSecondary,
        secondaryContainer = md_dark_secondaryContainer,
        onSecondaryContainer = md_dark_onSecondaryContainer,
        tertiary = md_dark_tertiary,
        onTertiary = md_dark_onTertiary,
        tertiaryContainer = md_dark_tertiaryContainer,
        onTertiaryContainer = md_dark_onTertiaryContainer,
        background = md_dark_background,
        onBackground = md_dark_onBackground,
        surface = md_dark_surface,
        onSurface = md_dark_onSurface,
        surfaceVariant = md_dark_surfaceVariant,
        onSurfaceVariant = md_dark_onSurfaceVariant,
        surfaceTint = md_dark_surfaceTint,
        inverseSurface = md_dark_inverseSurface,
        inverseOnSurface = md_dark_inverseOnSurface,
        error = md_dark_error,
        onError = md_dark_onError,
        errorContainer = md_dark_errorContainer,
        onErrorContainer = md_dark_onErrorContainer,
        outline = md_dark_outline,
        outlineVariant = md_dark_outlineVariant,
        scrim = md_dark_scrim,
    )

private val lightColorScheme =
    lightColorScheme(
        primary = md_light_primary,
        onPrimary = md_light_onPrimary,
        primaryContainer = md_light_primaryContainer,
        onPrimaryContainer = md_light_onPrimaryContainer,
        inversePrimary = md_light_inversePrimary,
        secondary = md_light_secondary,
        onSecondary = md_light_onSecondary,
        secondaryContainer = md_light_secondaryContainer,
        onSecondaryContainer = md_light_onSecondaryContainer,
        tertiary = md_light_tertiary,
        onTertiary = md_light_onTertiary,
        tertiaryContainer = md_light_tertiaryContainer,
        onTertiaryContainer = md_light_onTertiaryContainer,
        background = md_light_background,
        onBackground = md_light_onBackground,
        surface = md_light_surface,
        onSurface = md_light_onSurface,
        surfaceVariant = md_light_surfaceVariant,
        onSurfaceVariant = md_light_onSurfaceVariant,
        surfaceTint = md_light_surfaceTint,
        inverseSurface = md_light_inverseSurface,
        inverseOnSurface = md_light_inverseOnSurface,
        error = md_light_error,
        onError = md_light_onError,
        errorContainer = md_light_errorContainer,
        onErrorContainer = md_light_onErrorContainer,
        outline = md_light_outline,
        outlineVariant = md_light_outlineVariant,
        scrim = md_light_scrim,
    )

fun isDarkTheme(context: Context): Boolean {
    val uiMode = context.applicationContext.resources.configuration.uiMode
    val uiDark = Configuration.UI_MODE_NIGHT_YES
    return when (preferenceHandler.themeType) {
        AppThemeType.SYSTEM -> uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == uiDark
        AppThemeType.DARK -> true
        else -> false
    }
}

@Composable
fun ChoutenTheme(
    darkTheme: Boolean = isDarkTheme(LocalContext.current),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme =
        when {
            preferenceHandler.isDynamicColor && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                    context
                )
            }

            darkTheme -> darkColorScheme
            else -> lightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.navigationBarColor =
                colorScheme.surfaceColorAtElevation(3.dp)
                    .toArgb() // TODO: replace with new elevation system
            (view.context as Activity).window.statusBarColor = Color.Transparent.toArgb()
            // Set statusbar icons color considering the top app state banner
            val isIncognito = preferenceHandler.isIncognito
            val isOfflineMode = preferenceHandler.isOfflineMode

            val statusBarBackgroundColor = when {
                isOfflineMode -> colorScheme.tertiary
                isIncognito -> colorScheme.primary
                else -> colorScheme.surface
            }

            val isLightStatusBar = statusBarBackgroundColor.luminance() > 0.5
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars =
                isLightStatusBar
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
