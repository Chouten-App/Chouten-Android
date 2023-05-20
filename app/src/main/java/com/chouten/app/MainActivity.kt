package com.chouten.app

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.chouten.app.data.DataLayer
import com.chouten.app.data.LogDataLayer
import com.chouten.app.data.ModuleDataLayer
import com.chouten.app.data.NavigationItems
import com.chouten.app.ui.BottomNavigationBar
import com.chouten.app.ui.Navigation
import com.chouten.app.ui.theme.ChoutenTheme
import com.chouten.app.data.SnackbarVisualsWithError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.chouten.app.ui.components.Snackbar

lateinit var ModuleLayer: ModuleDataLayer
lateinit var LogLayer: LogDataLayer
val PrimaryDataLayer = DataLayer()

const val PREFERENCE_FILE = "CHOUTEN_PREFS"

lateinit var preferenceHandler: PreferenceManager

lateinit var App: MainActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App = this@MainActivity
        initializeNetwork(applicationContext)
        preferenceHandler = PreferenceManager(this)
        ModuleLayer = ModuleDataLayer()
        LogLayer = LogDataLayer()

        createAppDirectory()
        lifecycleScope.launch(Dispatchers.IO) {
            ModuleLayer.loadModules(applicationContext)
        }

        if (intent != null) handleSharedIntent(intent)
        installSplashScreen()
        setContent {
            ChoutenTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = PrimaryDataLayer.isNavigationShown,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        )
                        {
                            BottomNavigationBar(
                                navController = navController,
                                items = listOf(
                                    NavigationItems.HomePage,
                                    NavigationItems.SearchPage,
                                    NavigationItems.MorePage,
                                ),
                                onItemClick = {
                                    navController.navigate(route = it.route)
                                }
                            )
                        }
                    },
                    snackbarHost = { Snackbar() },
                    content = { padding ->
                        Box(
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize()
                        ) {
                            Navigation(navController)
                        }
                    }
                )
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
                        this,
                        intent
                    )

                    "application/json" -> ModuleLayer.enqueueFileInstall(
                        intent,
                        applicationContext
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
