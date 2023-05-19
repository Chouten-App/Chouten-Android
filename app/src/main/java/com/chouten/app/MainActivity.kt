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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.chouten.app.ui.theme.shapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val snackbarState = SnackbarHostState()
        val snackbarHost: @Composable () -> Unit = {
            SnackbarHost(hostState = snackbarState) { data ->
                val extendedVisuals = data.visuals as? SnackbarVisualsWithError
                val isError =
                    extendedVisuals?.isError
                        ?: false
                val buttonColor = if (isError) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }

                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        10.dp
                    ),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    action = {
                        TextButton(
                            onClick = {
                                extendedVisuals?.customButton?.action?.invoke()
                                    ?: if (isError) data.dismiss() else data.performAction()
                            },
                            shape = shapes.extraSmall,
                            colors = buttonColor
                        ) {
                            extendedVisuals?.customButton?.actionText?.let {
                                Text(
                                    it
                                )
                            } ?: extendedVisuals?.buttonText?.let {
                                Text(
                                    it
                                )
                            } ?: Icon(Icons.Default.Close, "Dismiss")
                        }
                    }
                ) {
                    Text(
                        text = data.visuals.message,
                        maxLines = 8
                    )
                }
            }
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
                    snackbarHost = snackbarHost,
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

        // Observe the Flow of the Snackbar Queue
        PrimaryDataLayer.snackbarQueue.observe(this) {
            it.forEach { snackbarItem ->
                lifecycleScope.launch { snackbarState.showSnackbar(snackbarItem) }
                PrimaryDataLayer.popSnackbarQueue()
            }
        }
    }

    private fun handleSharedIntent(intent: Intent?) {
        when (ACTION_SEND) {
            intent?.action -> {
                Log.d("INTENT", "$intent")
                // Enqueue the Resource
                when (intent.type) {
                    "text/plain" -> ModuleLayer.enqueueRemoteInstall(this, intent)
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
