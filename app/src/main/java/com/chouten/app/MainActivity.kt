package com.chouten.app

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.chouten.app.ui.theme.ChoutenTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PREFERENCE_FILE = "CHOUTEN_PREFS"

lateinit var preferenceHandler: PreferenceManager

lateinit var App: MainActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App = this@MainActivity
        initializeNetwork(applicationContext)
        initializeRepositories()
        preferenceHandler = PreferenceManager(this)

        createAppDirectory()
        lifecycleScope.launch(Dispatchers.IO) {
            ModuleLayer.loadModules(applicationContext)
        }

        if (intent != null) handleSharedIntent(intent)
        installSplashScreen()
        setContent {
            ChoutenTheme {
                ChoutenApp()
            }
        }
    }

    private fun handleSharedIntent(intent: Intent?) {
        when (ACTION_SEND) {
            intent?.action -> {
                Log.d("INTENT", "$intent")
                // Enqueue the Resource
                when (intent.type) {
                    "text/plain" -> ModuleLayer.enqueueRemoteInstall(
                        this, intent
                    )

                    "application/json" -> ModuleLayer.enqueueFileInstall(
                        intent, applicationContext
                    )

                    else -> Log.d(
                        "IMPORT",
                        "Import type `${intent.type}` not yet implemented"
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleSharedIntent(intent)
    }
}
