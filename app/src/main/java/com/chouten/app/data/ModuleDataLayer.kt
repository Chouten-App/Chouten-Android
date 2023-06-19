package com.chouten.app.data

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
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
import com.chouten.app.client
import com.chouten.app.get
import com.chouten.app.preferenceHandler
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class ModuleDataLayer {

    var selectedModule by mutableStateOf<ModuleModel?>(null)
    var availableModules = mutableStateListOf<ModuleModel>()
        private set

    private val bloomFilter = BloomFilter.create(
        Funnels.integerFunnel(), 100, 0.05
    )

    val webviewHandler = WebviewHandler()

    private fun reInitialize(context: Context) {
        webviewHandler.reset(context)
    }

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

            // Check if the module folder was nested
            // in a folder
            var moduleFolder = File(modulePath)
            val moduleFolderContents = moduleFolder.listFiles()
            if (moduleFolderContents?.size == 1) {
                val nestedFolder = moduleFolderContents[0]
                if (nestedFolder.isDirectory) {
                    nestedFolder.renameTo(moduleFolder)
                    moduleFolder = nestedFolder
                }
            }

            val module = getMetadata(modulePath)

            if (isModuleExisting(module)) {
                // remove the .cache folder
                moduleFolder.deleteRecursively()

                throw IOException("Module already exists")
            }

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
        println("Folder name is $folderUrl")
        val metadataFile = File("$folderUrl/metadata.json")

        if (!metadataFile.exists()) throw IOException("Metadata file does not exist")

        val metadata = metadataFile.readText()
        val module = Mapper.parse<ModuleModel>(metadata)
        module.meta.icon =
            "${folderUrl.substring(0, folderUrl.lastIndexOf("/"))}/${module.name}/icon.png"
        return module
    }

    /**
     * @param module The module to read the code from
     * @param subFolder The subfolder to read the code from (e.g. "Search", "Info", "Media")
     * @return A list of codeblocks
     */
    private suspend fun getModuleCode(
        module: ModuleModel,
        subFolder: String
    ): List<ModuleModel.ModuleCode.ModuleCodeblock> {
        val moduleDir =
            (AppPaths.addedDirs["Modules"]?.absolutePath + "/" + (module.name)) + "/$subFolder"

        val codeblocks = mutableListOf<ModuleModel.ModuleCode.ModuleCodeblock>()

        // get files that end with .js and sor them by the number in the file name. (e.g. code.js, code1.js, code2.js)
        val files = File(moduleDir).listFiles { _, name -> name.endsWith(".js") }
            ?.sortedWith { o1, o2 -> // sortedBy somehow doesn't work on android 9 which causes episodes to be empty
                val o1Number =
                    o1.name.substringAfter("code").substringBefore(".js").toIntOrNull() ?: 0
                val o2Number =
                    o2.name.substringAfter("code").substringBefore(".js").toIntOrNull() ?: 0
                o1Number.compareTo(o2Number)
            }
            ?: throw IOException("No Search files found")

        println(files)

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
                usesApi = false
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
        if (intent.clipData == null) return

        val fileUrl = intent.clipData?.getItemAt(0)?.uri?.toString() ?: return

        val modulePath =
            AppPaths.addedDirs["Modules"]?.absolutePath + "/" + (fileUrl.toUri().lastPathSegment?.removeSuffix(
                ".module"
            )
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + ".cache")

        try {

            // create a temporary file
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("checkout", ".zip", context.cacheDir)
            }

            withContext(Dispatchers.IO) {
                context.applicationContext.contentResolver.openInputStream(fileUrl.toUri())?.use {
                    // Write the bytes into a buffer
                    // and then use a buffered writer to write the bytes to the file
                    val buffer = ByteArray(4 * 1024)
                    val bufferedOutputStream = BufferedOutputStream(FileOutputStream(tempFile))
                    var read: Int
                    while (it.read(buffer).also { read = it } != -1) {
                        bufferedOutputStream.write(buffer, 0, read)
                    }
                    bufferedOutputStream.flush()

                    // close the streams
                    bufferedOutputStream.close()
                    it.close()
                }
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

    fun removeModule(module: ModuleModel) {
        try {
            val modulePath = AppPaths.addedDirs["Modules"]?.absolutePath + "/" + module.name
            val moduleFolder = File(modulePath)
            moduleFolder.deleteRecursively()
            availableModules.remove(module)

            // TODO: Are we going to select the next module in the list? Or just deselect?
            if (selectedModule == module) {
                selectedModule = null
                preferenceHandler.selectedModule = null
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
        selectedModule = module
        preferenceHandler.selectedModule = module.toString()
        println("Updating to ${module.name}")

        val moduleHome = App.lifecycleScope.async {
            getModuleCode(module, "Home")
        }

        val moduleSearch = App.lifecycleScope.async {
            getModuleCode(module, "Search")
        }

        val moduleInfo = App.lifecycleScope.async {
            getModuleCode(module, "Info")
        }

        val moduleMediaConsume = App.lifecycleScope.async {
            getModuleCode(module, "Media")
        }

        val home = moduleHome.await()
        val search = moduleSearch.await()
        val info = moduleInfo.await()
        val mediaConsume = moduleMediaConsume.await()
        module.code = mapOf(
            "anime" to ModuleModel.ModuleCode(
                search = search,
                home = home,
                info = info,
                mediaConsume = mediaConsume
            )
        )

        selectedModule = module
    }

    suspend fun loadModules() {
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
                    Log.d("METADATA", "loadModules: $metadata")
                    val decoded = Mapper.parse<ModuleModel>(metadata)
                    decoded.meta.icon = "${file.absolutePath}/icon.png"

                    availableModules.add(decoded)
                }
            }

            if (preferenceHandler.selectedModule?.isBlank() != true) {
                (Mapper.parse<ModuleModel>(preferenceHandler.selectedModule!!)).let { selected ->
                    availableModules.forEach { module ->
                        if (module.id == selected.id) {
                            //update the selected module
                            App.lifecycleScope.launch {
                                updateSelectedModule(module.id)
                            }
                            return@forEach
                        }
                    }
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
            if (!hasPermission) {
                removeModule(module)
                return@withContext
            }
            saveModule(context, module)
            bloomFilter.put(module.hashCode())
            availableModules += module
        }
    }
}
