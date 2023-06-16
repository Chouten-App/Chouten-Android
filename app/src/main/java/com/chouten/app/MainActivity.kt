package com.chouten.app

import android.content.ClipData
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.chouten.app.data.DataLayer
import com.chouten.app.ui.components.DownloadedOnlyBannerBackgroundColor
import com.chouten.app.ui.components.IncognitoModeBannerBackgroundColor
import com.chouten.app.ui.theme.ChoutenTheme
import com.chouten.app.ui.theme.isDarkTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PREFERENCE_FILE = "CHOUTEN_PREFS"

lateinit var preferenceHandler: PreferenceManager

lateinit var App: MainActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        App = this@MainActivity

        actionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        preferenceHandler = PreferenceManager(this)

        checkPermissions()
        createAppDirectory()

        initializeNetwork(applicationContext)
        initializeRepositories()

        lifecycleScope.launch(Dispatchers.IO) {
            ModuleLayer.loadModules()
        }

        if (intent != null) handleSharedIntent(intent)
        setContent {
            val incognito = preferenceHandler.isIncognito
            val downloadOnly = preferenceHandler.isOfflineMode
            // Set statusbar color considering the top app state banner
            val systemUiController = rememberSystemUiController()
            val statusBarBackgroundColor = when {
                downloadOnly -> DownloadedOnlyBannerBackgroundColor
                incognito -> IncognitoModeBannerBackgroundColor
                else -> MaterialTheme.colorScheme.surface
            }
            LaunchedEffect(systemUiController, statusBarBackgroundColor) {
                val isDarkTheme = isDarkTheme(App.applicationContext)
                val luminance = statusBarBackgroundColor.luminance()
                val darkIcons = if (isDarkTheme) luminance < 0.5 else luminance > 0.5

                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = darkIcons,
                    transformColorForLightContent = { Color.Black }
                )
            }

            ChoutenTheme {
                ChoutenApp()
            }
        }
    }

    private fun handleSharedIntent(intent: Intent?) {
        Log.d("INTENT", "$intent")
        // Enqueue the Resource
        lifecycleScope.launch {
            when (intent?.type) {
                "text/plain" -> ModuleLayer.enqueueRemoteInstall(
                    this@MainActivity, intent
                )

                "application/octet-stream" ->
                    ModuleLayer.enqueueFileInstall(
                        intent, this@MainActivity
                    )

                else -> Log.d(
                    "IMPORT",
                    "Import type `${intent?.type}` not yet implemented"
                )
            }

            // If the file is opened in a file manager, the intent will be null
            // so we need to check for that
            if (intent != null && intent.action == ACTION_VIEW) {
                val contentUrl = intent.dataString
                // Set the clipdata to the content url
                val clipData = ClipData.newRawUri("Content URL", contentUrl?.toUri())
                intent.clipData = clipData
                ModuleLayer.enqueueFileInstall(
                    intent, this@MainActivity
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleSharedIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Destroying Activity")
        ModuleLayer.webviewHandler.destroy()
    }
}
