package com.chouten.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.chouten.app.data.AppThemeType
import com.chouten.app.data.CustomDNS
import com.chouten.app.data.Preferences
import kotlin.reflect.KProperty

class PreferenceManager(context: Context) :
    BasePreferenceManager(
        context.applicationContext.getSharedPreferences(
            PREFERENCE_FILE,
            Context.MODE_PRIVATE
        )
    ) {
    var isDynamicColor: Boolean by booleanPreference(
        Preferences.Settings.dynamicColor.preference.first,
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    )

    var selectedModule by intPreference(
        Preferences.SelectedModule,
        -1
    )

    var themeType by enumPreference(
        Preferences.Settings.themeType.preference.first,
        AppThemeType.SYSTEM
    )

    var dns by enumPreference(
        Preferences.Settings.dns.preference.first,
        CustomDNS.NONE
    )
}

abstract class BasePreferenceManager(private val handler: SharedPreferences) {

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        handler.getBoolean(key, defaultValue)

    fun putBoolean(key: String, value: Boolean) =
        handler.edit { putBoolean(key, value) }

    fun getString(key: String, defaultValue: String?): String? =
        handler.getString(key, defaultValue)

    fun putString(key: String, value: String?): Unit =
        handler.edit { putString(key, value) }

    fun getInt(key: String, defaultValue: Int): Int =
        handler.getInt(key, defaultValue)

    fun putInt(key: String, value: Int): Unit =
        handler.edit { putInt(key, value) }

    fun getFloat(key: String, defaultValue: Float): Float =
        handler.getFloat(key, defaultValue)

    fun putFloat(key: String, value: Float): Unit =
        handler.edit { putFloat(key, value) }

    fun getLong(key: String, defaultValue: Long): Long =
        handler.getLong(key, defaultValue)

    fun putLong(key: String, value: Long): Unit =
        handler.edit { putLong(key, value) }

    inline fun <reified T : Enum<T>> getEnum(key: String, defaultValue: T): T =
        enumValueOf(getString(key, defaultValue.name)!!)

    inline fun <reified T : Enum<T>> putEnum(key: String, value: T) =
        putString(key, value.name)

    protected class Preference<T>(
        private val key: String,
        defaultValue: T,
        getter: (key: String, defaultValue: T) -> T,
        private val setter: (key: String, value: T) -> Unit
    ) {
        var value by mutableStateOf(getter(key, defaultValue))
            private set

        operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
        operator fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            updatedValue: T
        ) {
            value = updatedValue
            setter(key, updatedValue)
        }
    }

    protected fun booleanPreference(
        key: String,
        defaultValue: Boolean
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = { _, _ -> getBoolean(key, defaultValue) },
        setter = { _, value -> putBoolean(key, value) }
    )

    protected fun stringPreference(
        key: String,
        defaultValue: String = ""
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getString,
        setter = ::putString
    )

    protected fun intPreference(
        key: String,
        defaultValue: Int
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getInt,
        setter = ::putInt
    )

    protected fun floatPreference(
        key: String,
        defaultValue: Float
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getFloat,
        setter = ::putFloat
    )

    protected fun longPreference(
        key: String,
        defaultValue: Long
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getLong,
        setter = ::putLong
    )

    protected inline fun <reified T : Enum<T>> enumPreference(
        key: String,
        defaultValue: T
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getEnum,
        setter = ::putEnum
    )
}