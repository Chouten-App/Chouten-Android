package com.chouten.app.ui.views.settingsPage.screens.appearancePage

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.AppPaths
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.ui.views.settingsPage.json
import kotlinx.serialization.encodeToString
import kotlin.reflect.full.declaredMemberProperties

class AppearanceViewModel(context: Context, colorScheme: ColorScheme) : ViewModel() {

    private val themes: Pair<ColorScheme, ColorScheme>?
    private val surfaceContainer: Color

    init {
        this.surfaceContainer = colorScheme.surfaceColorAtElevation(3.dp)
        if (Build.VERSION.SDK_INT >= 31) {
            this.themes = Pair(
                dynamicLightColorScheme(context), dynamicDarkColorScheme(context)
            )
        } else {
            this.themes = null
        }
    }

    fun exportTheme(themeName: String): Boolean {
        if (themes == null) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    "Android 12 or higher is required to export themes", false
                )
            )
            return false
        }
        val getColours: ((ColorScheme) -> Map<String, String>) = { theme ->
            theme::class.declaredMemberProperties.associate { member ->
                val hexColor =
                    Integer.toHexString((member.getter.call(theme) as Color).toArgb()).drop(2)
                if (member.name.startsWith("on")) {
                    member.name to "#$hexColor"
                } else {
                    member.name.replaceFirstChar { it.uppercase() } to "#$hexColor"
                }
            }
        }

        val lightPairs = getColours(themes.first).toMutableMap().apply {
            this["SurfaceContainer"] = Integer.toHexString(surfaceContainer.toArgb()).drop(2)
        }
        val darkPairs = getColours(themes.second).toMutableMap().apply {
            this["SurfaceContainer"] = Integer.toHexString(surfaceContainer.toArgb()).drop(2)
        }

        val exportedJson = json.encodeToString(
            mapOf(
                "light" to lightPairs, "dark" to darkPairs
            )
        )

        val themeDir = AppPaths.addedDirs.getOrElse("Themes") {
            throw Exception("Themes directory not found")
        }

        val export = {
            val themeFile = themeDir.resolve("${themeName.trim()}.theme")
            themeFile.createNewFile()
            themeFile.writeText(exportedJson)
            true
        }

        return try {
            export()
        } catch (e: Exception) {
            // Create the app folders
            themeDir.mkdir()
            try {
                export()
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}