package com.chouten.app.data

import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.chouten.app.Mapper
import com.chouten.app.client
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.runBlocking

class ModuleDataLayer() {

    var selectedModule by mutableStateOf<ModuleModel?>(null)
        private set
    var availableModules = mutableStateListOf<ModuleModel>()
        private set

    fun enqueueRemoteInstall(url: String) {
        // TODO: Make async / use seperate service
        runBlocking {
            try {
                availableModules += client.get(url).parsed<ModuleModel>()
            } catch (e: Exception) {
                // TODO: Display nice Snackbar error / custom stylised toast
                e.localizedMessage?.let { Log.e("MODULE INSTALL", it) }
            }
        }
    }

    fun enqueueRemoteInstall(intent: Intent) {
        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!URLUtil.isNetworkUrl(url) || url == null) return
        enqueueRemoteInstall(url)
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

                try {
                    availableModules += Mapper.parse<ModuleModel>(json.toString())
                } catch (e: Exception) {
                    // TODO: Nice stylised toast / something
                    e.localizedMessage?.let { Log.e("IMPORT ERROR", it) }
                }

                inputStream?.close()

                // Use the URI to access the shared file
            }
        }
    }

    fun updateSelectedModule(module: ModuleModel) {
        println("Updating to ${module.name}")
        selectedModule = module
    }
}