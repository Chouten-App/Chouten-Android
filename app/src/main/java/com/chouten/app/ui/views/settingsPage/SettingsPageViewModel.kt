package com.chouten.app.ui.views.settingsPage

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.chouten.app.data.ChoutenSetting
import com.chouten.app.data.Preferences
import com.chouten.app.preferenceEditor
import com.chouten.app.preferenceHandler
import kotlin.reflect.full.declaredMemberProperties

enum class SettingType {
    ENUM,
    TOGGLE,
    VALUE
}

class SettingsPageViewModel() : ViewModel() {

    private val _settings = mutableStateMapOf<String, Any>()
    val settings: MutableMap<String, Any>
        get() = _settings

    init {
        Preferences.Settings::class.declaredMemberProperties.forEach { prop ->
            val prefType =
                ((prop.getter.call(Preferences.Settings) as ChoutenSetting).preference as Pair<*, *>).second
            settings[prop.name] = when (prefType) {
                is Boolean.Companion -> {
                    preferenceHandler.getBoolean(prop.name, false)
                }

                is Int.Companion -> {
                    preferenceHandler.getInt(prop.name, -1)
                }

                is Long.Companion -> {
                    preferenceHandler.getLong(prop.name, -1)
                }

                is Float.Companion -> {
                    preferenceHandler.getFloat(prop.name, -1f)
                }

                is String.Companion -> {
                    preferenceHandler.getString(prop.name, "").toString()
                }

                else -> {
                    throw Error("Type $prefType cannot be used within preferences!")
                }
            }
        }
    }

    fun toggleSetting(
        settingID: String,
        settingType: SettingType = SettingType.TOGGLE
    ) {
        when (settingType) {
            SettingType.TOGGLE -> {
                settings[settingID] =
                    !((settings[settingID] ?: false) as Boolean)
                preferenceEditor.putBoolean(
                    settingID,
                    (settings[settingID] ?: true) as Boolean
                )
            }

            else -> null
        }?.commit()
    }

    internal inline fun <reified T> getSetting(settingID: String): T {
        return when (val ret = settings[settingID]) {
            is T -> ret
            else -> throw Error("Key $settingID is not of the correct type")
        }
    }
}