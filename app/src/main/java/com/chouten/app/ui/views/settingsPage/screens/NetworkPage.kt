package com.chouten.app.ui.views.settingsPage.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chouten.app.App
import com.chouten.app.data.CustomDNS
import com.chouten.app.data.Preferences
import com.chouten.app.initializeNetwork
import com.chouten.app.preferenceHandler
import com.chouten.app.ui.components.SettingsChoice

@Composable
fun NetworkPage() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsChoice(
            preference = Preferences.Settings.dns,
            onPreferenceChange = { updated ->
                preferenceHandler.dns = updated
                // We need to remake the client
                initializeNetwork(App.applicationContext)
            },
            defaultValue = preferenceHandler.getEnum(
                Preferences.Settings.dns.preference.first,
                CustomDNS.NONE
            ),
        )
    }
}