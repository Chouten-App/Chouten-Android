package com.chouten.app.data

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

fun String.toColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    println("Failed to parse color: $this")
    Color.Transparent
}

@Serializable
data class ThemeModel(
    val Background: String,
    val Error: String,
    val ErrorContainer: String,
    val InverseOnSurface: String,
    val InversePrimary: String,
    val InverseSurface: String,
    val onBackground: String,
    val onError: String,
    val onErrorContainer: String,
    val onPrimary: String,
    val onPrimaryContainer: String,
    val onSecondary: String,
    val onSecondaryContainer: String,
    val onSurface: String,
    val onSurfaceVariant: String,
    val onTertiary: String,
    val onTertiaryContainer: String,
    val Outline: String,
    val OutlineVariant: String,
    val Primary: String,
    val PrimaryContainer: String,
    val Scrim: String,
    val Secondary: String,
    val SecondaryContainer: String,
    val Surface: String,
    val SurfaceTint: String,
    val SurfaceVariant: String,
    val Tertiary: String,
    val TertiaryContainer: String,
) {
    fun get(): ColorScheme {
        return ColorScheme(
            primary = Primary.toColor(),
            onPrimary = onPrimary.toColor(),
            primaryContainer = PrimaryContainer.toColor(),
            onPrimaryContainer = onPrimaryContainer.toColor(),
            inversePrimary = InversePrimary.toColor(),
            secondary = Secondary.toColor(),
            onSecondary = onSecondary.toColor(),
            secondaryContainer = SecondaryContainer.toColor(),
            onSecondaryContainer = onSecondaryContainer.toColor(),
            tertiary = Tertiary.toColor(),
            onTertiary = onTertiary.toColor(),
            tertiaryContainer = TertiaryContainer.toColor(),
            onTertiaryContainer = onTertiaryContainer.toColor(),
            background = Background.toColor(),
            onBackground = onBackground.toColor(),
            surface = Surface.toColor(),
            onSurface = onSurface.toColor(),
            surfaceVariant = SurfaceVariant.toColor(),
            onSurfaceVariant = onSurfaceVariant.toColor(),
            surfaceTint = SurfaceTint.toColor(),
            inverseSurface = InverseSurface.toColor(),
            inverseOnSurface = InverseOnSurface.toColor(),
            error = Error.toColor(),
            onError = onError.toColor(),
            errorContainer = ErrorContainer.toColor(),
            onErrorContainer = onErrorContainer.toColor(),
            outline = Outline.toColor(),
            outlineVariant = OutlineVariant.toColor(),
            scrim = Scrim.toColor(),
        )
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}