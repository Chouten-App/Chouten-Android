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
        preferenceHandler = PreferenceManager(this)

        App = this@MainActivity
        initializeNetwork(applicationContext)
        initializeRepositories()

        checkPermissions()
        createAppDirectory()
        lifecycleScope.launch(Dispatchers.IO) {
            ModuleLayer.loadModules()
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
                lifecycleScope.launch {
                    when (intent.type) {
                        "text/plain" -> ModuleLayer.enqueueRemoteInstall(
                            this@MainActivity, intent
                        )

                        "application/json" ->
                            ModuleLayer.enqueueFileInstall(
                                intent, this@MainActivity
                            )

                        else -> Log.d(
                            "IMPORT",
                            "Import type `${intent.type}` not yet implemented"
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleSharedIntent(intent)
    }
}
