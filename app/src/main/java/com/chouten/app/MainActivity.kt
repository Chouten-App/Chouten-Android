package com.chouten.app

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.chouten.app.data.ModuleDataLayer
import com.chouten.app.ui.theme.ChoutenTheme
import com.chouten.app.ui.views.homePage.HomePage
import com.chouten.app.ui.views.homePage.HomePageViewModel

lateinit var ModuleLayer: ModuleDataLayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNetwork(applicationContext)
        ModuleLayer = ModuleDataLayer(applicationContext)

        when (ACTION_SEND) {
            intent?.action -> {
                Log.d("INTENT", "$intent")
                // Enqueue the Resource
                ModuleLayer.enqueueRemoteInstall(intent)
            }
        }

        setContent {
            ChoutenTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomePage(HomePageViewModel())
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        when (ACTION_SEND) {
            intent?.action -> {
                Log.d("INTENT", "$intent")
                // Enqueue the Resource
                when (intent.type) {
                    "text/plain" -> ModuleLayer.enqueueRemoteInstall(intent)
                    "application/json" -> ModuleLayer.enqueueFileInstall(intent)
                    else -> Log.d(
                        "IMPORT",
                        "Import type `${intent.type}` not yet implemented"
                    )
                }
            }
        }
    }
}
