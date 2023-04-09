package com.chouten.app

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.chouten.app.data.DataLayer
import com.chouten.app.data.ModuleDataLayer
import com.chouten.app.ui.theme.ChoutenTheme
import com.chouten.app.ui.theme.SnackbarVisualsWithError
import com.chouten.app.ui.theme.shapes
import com.chouten.app.ui.views.homePage.HomePage
import kotlinx.coroutines.launch

lateinit var ModuleLayer: ModuleDataLayer
val PrimaryDataLayer = DataLayer()

const val PREFERENCE_FILE = "CHOUTEN_PREFS"

lateinit var preferenceHandler: SharedPreferences
lateinit var preferenceEditor: Editor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNetwork(applicationContext)
        preferenceHandler = applicationContext.getSharedPreferences(PREFERENCE_FILE, 0)
        preferenceEditor = preferenceHandler.edit()
        ModuleLayer = ModuleDataLayer()

        createAppDirectory()
        ModuleLayer.loadModules(applicationContext)

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
                        FilledTonalButton(
                            onClick = { if (isError) data.dismiss() else data.performAction() },
                            shape = shapes.extraSmall,
                            colors = buttonColor
                        ) { Text(extendedVisuals?.buttonText ?: "Dismiss") }
                    }
                ) {
                    Text("${if (isError) "Error" else "Info"}: ${data.visuals.message}")
                }
            }
        }

        if (intent != null) handleSharedIntent(intent)

        setContent {
            ChoutenTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem( // TODO: replace with array of items https://www.youtube.com/watch?v=6DQQceGHxC4
                                selected = true,
                                onClick = {
                                    PrimaryDataLayer.enqueueSnackbar(
                                        SnackbarVisualsWithError(
                                            "This is item 1",
                                            false,
                                        )
                                    )
                                          },
                                label = { Text("Home") },
                                icon = { Icon(Icons.Rounded.Home, null) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    // change the selected item
                                    PrimaryDataLayer.enqueueSnackbar(
                                        SnackbarVisualsWithError(
                                            "This is item 2",
                                            true,
                                        )
                                    )
                                },
                                label = { Text("Settings") },
                                icon = { Icon(Icons.Outlined.Settings, null) }
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
                            HomePage(
                                navController = rememberNavController(),
                                context = applicationContext,
                            )
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
