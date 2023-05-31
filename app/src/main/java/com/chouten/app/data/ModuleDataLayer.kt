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
import androidx.lifecycle.lifecycleScope
import com.chouten.app.App
import com.chouten.app.Mapper
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.UnzipUtils
import com.chouten.app.get
import com.chouten.app.client
import com.chouten.app.preferenceHandler
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.Locale

class ModuleDataLayer {

    var selectedModule by mutableStateOf<ModuleModel?>(null)
        private set
    var availableModules = mutableStateListOf<ModuleModel>()
        private set

    private val bloomFilter = BloomFilter.create(
        Funnels.integerFunnel(), 100, 0.05
    )

    private val webviewHandler = WebviewHandler()

    init {
        webviewHandler.initialize(App.applicationContext)
    }

    private fun isModuleExisting(module: ModuleModel): Boolean {
        availableModules.find { it.hashCode() == module.hashCode() }
            ?: return false
        return true
    }

    suspend fun enqueueRemoteInstall(context: Context, url: String) {
        if (!URLUtil.isNetworkUrl(url)) throw IOException("Invalid URL")

        val modulePath =
            AppPaths.addedDirs["Modules"]?.absolutePath + "/" + (url.toUri().lastPathSegment?.removeSuffix(
                ".module"
            )
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + ".cache")

        try {
            // download the zip file
            val response = client.get(url)
            val bytes = response.body.bytes()

            // create a temporary file
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("checkout", ".zip", context.cacheDir)
            }

            // write the bytes to the file
            withContext(Dispatchers.IO) {
                tempFile.writeBytes(bytes)
            }

            // unzip the file
            withContext(Dispatchers.IO) {
                UnzipUtils.unzip(tempFile, modulePath)
                tempFile.delete()
            }

            val module = getMetadata(modulePath)

            if (isModuleExisting(module)) {
                // remove the .cache folder
                val moduleFolder = File(modulePath)
                moduleFolder.deleteRecursively()

                throw IOException("Module already exists")
            }

            // rename the folder to the module name
            val moduleFolder = File(modulePath)
            val newModuleFolder = File(moduleFolder.parent, module.name)
            moduleFolder.renameTo(newModuleFolder)

            // Use MediaScannerConnection to scan the file so that it shows up in the file manager
            MediaScannerConnection.scanFile(
                context,
                arrayOf(newModuleFolder.absolutePath),
                null,
                null
            )

            addModule(context, module)

        } catch (e: Exception) {
            // remove the temporary file if it exists
            if (e is IOException) {
                val tempFile = File(context.cacheDir, "checkout.zip")
                if (tempFile.exists()) tempFile.delete()

                // delete .cache folder if it exists
                val moduleFolder = File(modulePath)
                if (moduleFolder.exists()) moduleFolder.deleteRecursively()
            }

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

    private fun getMetadata(folderUrl: String): ModuleModel {
        val metadataFile = File("$folderUrl/metadata.json")

        if (!metadataFile.exists()) throw IOException("Metadata file does not exist")

        val metadata = metadataFile.readText()
        val module = Mapper.parse<ModuleModel>(metadata)
        module.meta.icon =
            "${folderUrl.substring(0, folderUrl.lastIndexOf("/"))}/${module.name}/icon.png"
        return module
    }

    // TODO: Move (getInfoCode, getSearchCode, getHomeCode) to a single function to reduce code duplication
    private suspend fun getInfoCode(module: ModuleModel): List<ModuleModel.ModuleCode.ModuleCodeblock> {
        val moduleDir =
            (AppPaths.addedDirs["Modules"]?.absolutePath + "/" + (module.name)) + "/Info"

        val codeblocks = mutableListOf<ModuleModel.ModuleCode.ModuleCodeblock>()

        // get files that end with .js and sor them by the number in the file name. (e.g. code.js, code1.js, code2.js)
        val files = File(moduleDir).listFiles { _, name -> name.endsWith(".js") }
            ?.sortedBy { it.name.substringBefore(".js").toIntOrNull() ?: 0 }
            ?: throw IOException("No Home files found")

        files.forEach {
            val code = it.readText()
            val requestData = getRequestData(code)

            codeblocks.add(
                element = ModuleModel.ModuleCode.ModuleCodeblock(
                    code = "function logic() ${code.substringAfter("function logic()")}; logic();",
                    removeScripts = requestData.removeScripts,
                    allowExternalScripts = requestData.allowExternalScripts,
                    usesApi = requestData.usesApi,
                    request = requestData.request,
                    imports = requestData.imports
                )
            )
        }
        return codeblocks
    }

    private suspend fun getHomeCode(module: ModuleModel): List<ModuleModel.ModuleCode.ModuleCodeblock> {
        val moduleDir =
            (AppPaths.addedDirs["Modules"]?.absolutePath + "/" + (module.name)) + "/Home"

        val codeblocks = mutableListOf<ModuleModel.ModuleCode.ModuleCodeblock>()

        // get files that end with .js and sor them by the number in the file name. (e.g. code.js, code1.js, code2.js)
        val files = File(moduleDir).listFiles { _, name -> name.endsWith(".js") }
            ?.sortedBy { it.name.substringBefore(".js").toIntOrNull() ?: 0 }
            ?: throw IOException("No Home files found")

        files.forEach {
            val code = it.readText()
            val requestData = getRequestData(code)

            codeblocks.add(
                element = ModuleModel.ModuleCode.ModuleCodeblock(
                    code = "function logic() ${code.substringAfter("function logic()")}; logic();",
                    removeScripts = requestData.removeScripts,
                    allowExternalScripts = requestData.allowExternalScripts,
                    usesApi = requestData.usesApi,
                    request = requestData.request,
                    imports = requestData.imports
                )
            )
        }
        return codeblocks
    }

    private suspend fun getSearchCode(module: ModuleModel): List<ModuleModel.ModuleCode.ModuleCodeblock> {
        val moduleDir =
            (AppPaths.addedDirs["Modules"]?.absolutePath + "/" + (module.name)) + "/Search"

        val codeblocks = mutableListOf<ModuleModel.ModuleCode.ModuleCodeblock>()

        // get files that end with .js and sor them by the number in the file name. (e.g. code.js, code1.js, code2.js)
        val files = File(moduleDir).listFiles { _, name -> name.endsWith(".js") }
            ?.sortedBy { it.name.substringBefore(".js").toIntOrNull() ?: 0 }
            ?: throw IOException("No Search files found")

        files.forEach {
            val code = it.readText()
            val requestData = getRequestData(code)
            codeblocks.add(
                element = ModuleModel.ModuleCode.ModuleCodeblock(
                    code = "function logic() ${code.substringAfter("function logic()")}; logic();",
                    removeScripts = requestData.removeScripts,
                    allowExternalScripts = requestData.allowExternalScripts,
                    usesApi = requestData.usesApi,
                    request = requestData.request,
                    imports = requestData.imports
                )
            )
        }
        return codeblocks
    }

    private suspend fun getRequestData(code: String): ModuleModel.ModuleCode.ModuleCodeblock {
        val request = webviewHandler.inject(
            ModuleModel.ModuleCode.ModuleCodeblock(
                code = code.substringBefore("function logic()") + "requestData();",
                removeScripts = false,
                allowExternalScripts = false,
                usesApi = true
            ),
            true
        )

        // the request data is returned as a stringified json object that looks like this:
        // "{\"request\":{\"url\": ...}}"
        // replace the escaped quotes with normal quotes and parse the json

        return Mapper.parse(request.replace("\\\\\"|\"\\{|\\}\"".toRegex()) {
            when (it.value) {
                "\\\"" -> "\""
                "\"{" -> "{"
                "}\"" -> "}"
                else -> it.value
            }
        })
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

    fun removeModule(module: ModuleModel) {
        try {
            val modulePath = AppPaths.addedDirs["Modules"]?.absolutePath + "/" + module.name
            val moduleFolder = File(modulePath)
            moduleFolder.deleteRecursively()
            availableModules.remove(module)

            // TODO: Are we going to select the next module in the list? Or just deselect?
            if (selectedModule == module) {
                selectedModule = null
                preferenceHandler.selectedModule = -1
            }

        } catch (e: Exception) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError(
                    e.localizedMessage ?: "Could not remove module",
                    true,
                )
            )
            e.localizedMessage?.let {
                Log.e("REMOVE MODULE ERROR", it)
                e.printStackTrace()
            }
        }
    }

    suspend fun updateSelectedModule(moduleId: String) {
        val module = availableModules[moduleId]
        println("Updating to ${module.name}")
        val lock = Mutex()

        val moduleHome = App.lifecycleScope.async {
            lock.withLock {
                getHomeCode(module)
            }
        }

        val moduleSearch = App.lifecycleScope.async {
            lock.withLock {
                getSearchCode(module)
            }
        }

        val moduleInfo = App.lifecycleScope.async {
            lock.withLock {
                getInfoCode(module)
            }
        }

        // TODO: Somehow having multiple async calls breaks. Need a fix
        // probably because its injecting multiple things at the same time


        // Wait for all the async calls to complete
        val home = moduleHome.await()
        module.code = mapOf("anime" to ModuleModel.ModuleCode(home = home))
//
//        val info = moduleInfo.await()
//        module.code = mapOf("anime" to ModuleModel.ModuleCode(home = home))
//
//        val search = moduleSearch.await()
//        module.code = mapOf("anime" to ModuleModel.ModuleCode(home = home))

        selectedModule = module
        preferenceHandler.selectedModule = selectedModule.hashCode()
    }

    fun loadModules(context: Context) {
        try {
            val modulesDir = AppPaths.addedDirs.getOrElse("Modules") {
                throw IOException("Modules folder not found")
            }

            modulesDir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    val metadataFile = File(file, "metadata.json")
                    if (!metadataFile.exists()) {
                        // remove the folder if the metadata file does not exist
                        file.deleteRecursively()
                        PrimaryDataLayer.enqueueSnackbar(
                            SnackbarVisualsWithError(
                                "Module ${file.name} does not have a metadata file. Removing...",
                                true,
                            )
                        )
                        return@forEach
                    }

                    val metadata = metadataFile.readText()
                    val decoded = Mapper.parse<ModuleModel>(metadata)
                    decoded.meta.icon = "${file.absolutePath}/icon.png"

                    availableModules.add(decoded)
                }
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

    private fun saveModule(context: Context, module: ModuleModel) {
        try {
            val modulesDir = AppPaths.addedDirs.getOrElse("Modules") {
                throw IOException("Modules folder not found")
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
            bloomFilter.put(module.hashCode())
            availableModules += module
        }
    }
}
