package com.chouten.app.data

import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.URLUtil
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.chouten.app.*
import com.chouten.app.ui.theme.SnackbarVisualsWithError
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.IOException

class ModuleDataLayer() {

    var selectedModule by mutableStateOf<ModuleModel?>(null)
        private set
    var availableModules = mutableStateListOf<ModuleModel>()
        private set

    private val bloomFilter = BloomFilter.create(
        Funnels.integerFunnel(),
        100,
        0.05
    )

    fun isModuleExisting(module: ModuleModel): Boolean {
        val doesNotExist = !bloomFilter.mightContain(module.hashCode())
        if (doesNotExist) return doesNotExist

        return availableModules.contains(module)
    }

    fun enqueueRemoteInstall(context: Context, url: String) {
        // TODO: Make async / use seperate service
        runBlocking {
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
    }

    fun enqueueRemoteInstall(context: Context, intent: Intent) {
        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!URLUtil.isNetworkUrl(url) || url == null) return
        enqueueRemoteInstall(context, url)
    }

    fun enqueueFileInstall(intent: Intent, context: Context) {

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
                while (reader.readLine().also { line = it } != null) {
                    json.append(line)
                }

                inputStream?.close()

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
                    e.localizedMessage?.let { Log.e("IMPORT ERROR", it) }
                }
            }
        }
    }

    fun updateSelectedModule(module: ModuleModel) {
        println("Updating to ${module.name}")
        selectedModule = module
    }

    fun loadModules(context: Context) {
        try {
            val modulesDir = AppPaths.addedDirs.getOrElse("Modules") {
                throw IOException("Modules folder not found")
            }

            val loadedModules = mutableListOf<ModuleModel>()
            val toLoad = mutableListOf<String>()

            val mediaStoreUri = MediaStore.Files.getContentUri("external")

            val projection: Array<String> = arrayOf(
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE
            )

            // Select all entries with mime type `application/json`
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            val selectionArgs = arrayOf("application/json")

            // Query the URI for all files of type `application/json`
            // within the ~/Documents/Chouten/Modules/ folder
            context.contentResolver.query(
                mediaStoreUri,
                projection,
                selection,
                selectionArgs,
                null
            ).use {
                if (it != null && it.moveToFirst()) {
                    do {
                        // Add the name of the file to the `toLoad` list
                        toLoad += it.getString(
                            it.getColumnIndexOrThrow(
                                MediaStore.Files.FileColumns.DISPLAY_NAME
                            )
                        )
                    } while (it.moveToNext())
                }
            }

            toLoad.forEach { file ->
                context.contentResolver.openInputStream(
                    File(
                        modulesDir,
                        file
                    ).toUri()
                ).use {
                    val reader = BufferedReader(InputStreamReader(it))
                    val json = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        json.append(line)
                    }

                    val module = Mapper.parse<ModuleModel>(json.toString())
                    module.id = availableModules.count() + loadedModules.count()
                    bloomFilter.put(module.hashCode())
                    loadedModules += module
                }
            }

            availableModules += loadedModules
            Log.d("CHOUTEN", "LOADED ${loadedModules.size} MODULES")

        } catch (e: IOException) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    e.localizedMessage ?: "Could not save Module",
                    true
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

            context.contentResolver.openOutputStream(moduleFile.toUri()).use {
                it?.write(Mapper.json.encodeToString(module).toByteArray())
            }

            // Add the MetaData to the MediaStore
            MediaScannerConnection.scanFile(
                context,
                arrayOf(moduleFile.path),
                arrayOf("application/json")
            ) { _, uri ->
                PrimaryDataLayer.enqueueSnackbar(
                    SnackbarVisualsWithError(
                        "Successfully saved Module",
                        false
                    )
                )
            }

        } catch (e: IOException) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    e.localizedMessage ?: "Could not save Module",
                    true
                )
            )

            e.localizedMessage?.let { Log.e("CHOUTEN", it) }
            e.printStackTrace()
        }
    }

    fun addModule(context: Context, module: ModuleModel) {
        saveModule(context, module)
        module.id = availableModules.count()
        bloomFilter.put(module.hashCode())
        availableModules += module
    }
}