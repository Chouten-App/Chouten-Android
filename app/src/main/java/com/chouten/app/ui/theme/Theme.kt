package com.chouten.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import com.chouten.app.R

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(R.color.darkPrimary),
        onPrimary = Color(R.color.darkOnPrimary),
        primaryContainer = Color(R.color.darkPrimaryContainer),
        onPrimaryContainer = Color(R.color.darkOnPrimaryContainer),
        inversePrimary = Color(R.color.darkInversePrimary),
        secondary = Color(R.color.darkSecondary),
        onSecondary = Color(R.color.darkOnSecondary),
        secondaryContainer = Color(R.color.darkSecondaryContainer),
        onSecondaryContainer = Color(R.color.darkOnSecondaryContainer),
        tertiary = Color(R.color.darkTertiary),
        onTertiary = Color(R.color.darkOnTertiary),
        tertiaryContainer = Color(R.color.darkTertiaryContainer),
        onTertiaryContainer = Color(R.color.darkOnTertiaryContainer),
        background = Color(R.color.darkBackground),
        onBackground = Color(R.color.darkOnBackground),
        surface = Color(R.color.darkSurface),
        onSurface = Color(R.color.darkOnSurface),
        surfaceVariant = Color(R.color.darkSurfaceVariant),
        onSurfaceVariant = Color(R.color.darkOnSurfaceVariant),
        surfaceTint = Color(R.color.darkSurfaceTint),
        inverseSurface = Color(R.color.darkInverseSurface),
        inverseOnSurface = Color(R.color.darkInverseOnSurface),
        error = Color(R.color.darkError),
        onError = Color(R.color.darkOnError),
        errorContainer = Color(R.color.darkErrorContainer),
        onErrorContainer = Color(R.color.darkOnErrorContainer),
        outline = Color(R.color.darkOutline),
        outlineVariant = Color(R.color.darkOutlineVariant),
        scrim = Color(R.color.darkScrim),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color(R.color.lightPrimary),
        onPrimary = Color(R.color.lightOnPrimary),
        primaryContainer = Color(R.color.lightPrimaryContainer),
        onPrimaryContainer = Color(R.color.lightOnPrimaryContainer),
        inversePrimary = Color(R.color.lightInversePrimary),
        secondary = Color(R.color.lightSecondary),
        onSecondary = Color(R.color.lightOnSecondary),
        secondaryContainer = Color(R.color.lightSecondaryContainer),
        onSecondaryContainer = Color(R.color.lightOnSecondaryContainer),
        tertiary = Color(R.color.lightTertiary),
        onTertiary = Color(R.color.lightOnTertiary),
        tertiaryContainer = Color(R.color.lightTertiaryContainer),
        onTertiaryContainer = Color(R.color.lightOnTertiaryContainer),
        background = Color(R.color.lightBackground),
        onBackground = Color(R.color.lightOnBackground),
        surface = Color(R.color.lightSurface),
        onSurface = Color(R.color.lightOnSurface),
        surfaceVariant = Color(R.color.lightSurfaceVariant),
        onSurfaceVariant = Color(R.color.lightOnSurfaceVariant),
        surfaceTint = Color(R.color.lightSurfaceTint),
        inverseSurface = Color(R.color.lightInverseSurface),
        inverseOnSurface = Color(R.color.lightInverseOnSurface),
        error = Color(R.color.lightError),
        onError = Color(R.color.lightOnError),
        errorContainer = Color(R.color.lightErrorContainer),
        onErrorContainer = Color(R.color.lightOnErrorContainer),
        outline = Color(R.color.lightOutline),
        outlineVariant = Color(R.color.lightOutlineVariant),
        scrim = Color(R.color.lightScrim),
    )

@Composable
fun ChoutenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                    context
                )
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor =
                colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars =
                darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
