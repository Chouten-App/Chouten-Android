package com.chouten.app.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.lifecycleScope
import com.chouten.app.App
import com.chouten.app.Mapper
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.preferenceHandler
import kotlinx.coroutines.launch
import java.io.IOException

// The first element is the name of the theme
// The second element is a pair of ThemeModels, with the first
// being the light theme and the second being the dark theme
typealias ChoutenTheme = Pair<String, Pair<ThemeModel, ThemeModel>>

class ThemeDataLayer {
    var selectedTheme by mutableStateOf<ChoutenTheme?>(null)
        private set
    var availableThemes = mutableStateListOf<ChoutenTheme>()
        private set

    fun setTheme(theme: String) {
        selectedTheme = availableThemes.find { it.first == theme }
        selectedTheme?.let {
            preferenceHandler.selectedTheme = it.hashCode()
        }
    }

    fun setTheme(theme: ChoutenTheme) {
        selectedTheme = theme
        preferenceHandler.selectedTheme = theme.hashCode()
    }

    fun getThemeNames(): List<String> {
        return listOf("Default") + availableThemes.map { it.first }
    }

    fun loadThemes() {
        try {
            val themesDir = AppPaths.addedDirs.getOrElse("Themes") {
                throw IOException("Themes folder not found")
            }

            val themes = mutableListOf<ChoutenTheme>()

            themesDir.listFiles { file ->
                file.extension == "theme"
            }?.forEach { theme ->
                val themeName = theme.nameWithoutExtension
                val themeJson = theme.readText()
                println(themeJson)
                val themeParts = Mapper.parse<Map<String, ThemeModel>>(themeJson)
                val themeLight = themeParts.getOrElse("light") {
                    throw IOException("Light theme not found")
                }
                val themeDark = themeParts.getOrElse("dark") {
                    throw IOException("Dark theme not found")
                }
                val choutenTheme = ChoutenTheme(themeName, Pair(themeLight, themeDark))
                themes.add(choutenTheme)
            }

            preferenceHandler.selectedModule.let { selected ->
                availableThemes.forEach { module ->
                    if (module.hashCode() == selected) {
                        //update the selected module
                        App.lifecycleScope.launch {
                            setTheme(module)
                        }
                        return@forEach
                    }
                }
            }

            availableThemes = themes.toMutableStateList()
        } catch (e: IOException) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    e.localizedMessage ?: "Could not save Theme", true
                )
            )

            e.localizedMessage?.let { Log.e("CHOUTEN", it) }
            e.printStackTrace()
        }
    }
}