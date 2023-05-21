package com.chouten.app.data

import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.chouten.app.Mapper
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.client
import com.chouten.app.preferenceHandler
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class ModuleDataLayer() {

    var selectedModule by mutableStateOf<ModuleModel?>(null)
        private set
    var availableModules = mutableStateListOf<ModuleModel>()
        private set

    private val bloomFilter = BloomFilter.create(
        Funnels.integerFunnel(), 100, 0.05
    )

    private fun isModuleExisting(module: ModuleModel): Boolean {
        // TODO: Fix bloom filter
        // Android Studio also gives some weird warning about `doesNotExist`
        // always being true which doesn't make much sense.
        // val doesNotExist = !bloomFilter.mightContain(module.hashCode())
        // if (doesNotExist) return doesNotExist

        availableModules.find { it.hashCode() == module.hashCode() }
            ?: return false
        return true
    }

    suspend fun enqueueRemoteInstall(context: Context, url: String) {
        try {
            val module = client.get(url).parsed<ModuleModel>()

            // At the moment we will not allow installs which are the same.
            // In the future, we may allow modules which have different versions
            // to be installed side by side.
            if (isModuleExisting(module)) throw IOException("Module already installed")

            addModule(context, module)
        } catch (e: Exception) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    e.localizedMessage ?: "Could not download module",
                    true,
                    // TODO: Add more details on button click
                )
            )
            e.localizedMessage?.let { Log.e("MODULE INSTALL", it) }
        }
    }

    suspend fun enqueueRemoteInstall(context: Context, intent: Intent) {
        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!URLUtil.isNetworkUrl(url) || url == null) return
        enqueueRemoteInstall(context, url)
    }

    suspend fun enqueueFileInstall(intent: Intent, context: Context) {
        if (intent.clipData != null) {
            val clipdata: ClipData = intent.clipData!!
            val itemCount: Int = clipdata.itemCount
            for (i in 0 until itemCount) {
                val uri: Uri = clipdata.getItemAt(i).uri

                val resolver: ContentResolver = context.contentResolver
                val inputStream = resolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = StringBuilder()
                var line: String?
                withContext(Dispatchers.IO) {
                    while (reader.readLine().also { line = it } != null) {
                        json.append(line)
                    }
                    inputStream?.close()
                }

                try {
                    val module = Mapper.parse<ModuleModel>(json.toString())

                    // At the moment we will not allow installs which are the same.
                    // In the future, we may allow modules which have different versions
                    // to be installed side by side.
                    if (isModuleExisting(module)) throw IOException("Module already installed")

                    addModule(context, module)
                } catch (e: Exception) {
                    PrimaryDataLayer.enqueueSnackbar(
                        SnackbarVisualsWithError(
                            e.localizedMessage ?: "Could not install module",
                            true,
                        )
                    )
                    e.localizedMessage?.let {
                        Log.e("IMPORT ERROR", it)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun updateSelectedModule(moduleId: Int) {
        val module = availableModules[moduleId]
        println("Updating to ${module.name}")
        selectedModule = module
        preferenceHandler.selectedModule = selectedModule.hashCode()
    }

    fun loadModules(context: Context) {
        try {
            val modulesDir = AppPaths.addedDirs.getOrElse("Modules") {
                throw IOException("Modules folder not found")
            }

            val loadedModules = mutableListOf<ModuleModel>()

            modulesDir.listFiles { _, name ->
                name.endsWith(".json")
            }?.forEach { file ->
                val reader =
                    BufferedReader(InputStreamReader(file.inputStream()))
                val json = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    json.append(line)
                }

                val module = Mapper.parse<ModuleModel>(json.toString())
                module.id = availableModules.count() + loadedModules.count()
                if (selectedModule == null && module.hashCode() == preferenceHandler.selectedModule) {
                    selectedModule = module
                }
                bloomFilter.put(module.hashCode())
                loadedModules += module
            }

            availableModules += loadedModules
            Log.d("CHOUTEN", "LOADED ${loadedModules.size} MODULES")

        } catch (e: IOException) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    e.localizedMessage ?: "Could not save Module", true
                )
            )

            e.localizedMessage?.let { Log.e("CHOUTEN", it) }
            e.printStackTrace()
        }
    }

    private fun saveModule(context: Context, module: ModuleModel) {
        try {
            val modulesDir = AppPaths.addedDirs.getOrElse("Modules") {
                throw IOException("Modules folder not found")
            }

            val moduleFile = File(
                modulesDir,
                "${module.name}_${module.meta.author}.${module.version}.json"
            )

            Log.d("CHOUTEN/IO", "$moduleFile")

            context.contentResolver.openOutputStream(moduleFile.toUri()).use {
                it?.write(Mapper.json.encodeToString(module).toByteArray())
            }

            // Add the Metadata to the MediaStore
            MediaScannerConnection.scanFile(
                context, arrayOf(moduleFile.path), arrayOf("application/json")
            ) { _, _ ->
                PrimaryDataLayer.enqueueSnackbar(
                    SnackbarVisualsWithError(
                        "Successfully saved Module", false
                    )
                )
            }

        } catch (e: IOException) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    e.localizedMessage ?: "Could not save Module", true
                )
            )

            e.localizedMessage?.let { Log.e("CHOUTEN", it) }
            e.printStackTrace()
        }
    }

    suspend fun addModule(context: Context, module: ModuleModel) {

        // Can the app install the module?
        var hasPermission = false
        // Has the user dismissed/accepted the permission?
        var hasUserPermission = false

        PrimaryDataLayer.enqueueAlert(
            AlertData(
                "Install ${module.name}?",
                "Are you sure want to install ${module.name}?\nWe cannot guarantee the safety of this module, so install at your own risk.",
                "Install",
                {
                    hasUserPermission = true
                    hasPermission = true
                },
                "Cancel",
                {
                    hasUserPermission = true
                }
            )
        )

        // We want to suspend until the user has accepted or declined the alert
        withContext(Dispatchers.IO) {
            while (!hasUserPermission) {
                delay(100)
            }
            if (!hasPermission) return@withContext
            saveModule(context, module)
            module.id = availableModules.count()
            bloomFilter.put(module.hashCode())
            availableModules += module
        }
    }
}