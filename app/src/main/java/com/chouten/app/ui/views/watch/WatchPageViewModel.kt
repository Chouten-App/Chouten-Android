package com.chouten.app.ui.views.watch

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.Mapper
import com.chouten.app.ModuleLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.ModuleResponse
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.data.WatchResult
import com.chouten.app.data.WebviewHandler
import java.net.URLDecoder
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class WatchPageViewModel(
        context: Context,
        private var title: String? = "",
        private var name: String? = "",
        _url: String
) : ViewModel() {
    var url: String = ""
        private set

    val webview = WebviewHandler()

    private val syncLock = Mutex(false)

    private var _mediaUrl by mutableStateOf("")
    val mediaUrl: String
        get() = _mediaUrl

    private var _servers by mutableStateOf(listOf<WatchResult.Server>())
    val servers: List<WatchResult.Server>
        get() = _servers

    private var _sources by mutableStateOf(listOf<WatchResult.Source>())
    val sources: List<WatchResult.Source>
        get() = _sources

    private var _subtitles by mutableStateOf(listOf<WatchResult.Subtitles>())
    val subtitles: List<WatchResult.Subtitles>
        get() = _subtitles

    private var _skips by mutableStateOf(listOf<WatchResult.SkipTimes>())
    val skips: List<WatchResult.SkipTimes>
        get() = _skips

    fun callback(message: String) {

        val res = message

        if (res.isBlank()) {
            PrimaryDataLayer.enqueueSnackbar(
                    SnackbarVisualsWithError("No results found for $title", false)
            )
            // return@launch
        }

        try {
            val results = Mapper.parse<ModuleResponse<List<WatchResult.Server>>>(res)
            // webview.updateNextUrl(results.nextUrl)
            println("Results for servers are ${results.result}")

            _servers = results.result
        } catch (e: Exception) {
            try {
                val results = Mapper.parse<ModuleResponse<WatchResult>>(res)
                // webview.updateNextUrl(results.nextUrl)
                println("Results for watch are ${results.result}")

                _sources = results.result.sources
                _subtitles = results.result.subtitles
                _skips = results.result.skips
            } catch (e: Exception) {
                e.printStackTrace()
                PrimaryDataLayer.enqueueSnackbar(
                        SnackbarVisualsWithError("Error parsing results for $title", false)
                )
                syncLock.unlock()
                // return@launch
            }
        }
        syncLock.unlock()
    }

    init {
        // Both title and url are url-encoded.
        title = URLDecoder.decode(title, "UTF-8")
        name = URLDecoder.decode(name, "UTF-8")
        val decodedUrl = URLDecoder.decode(_url, "UTF-8")
        url = _url

        // We want to get the info code from the webview handler
        // and then load the page with that code.
        val currentModule = ModuleLayer.selectedModule
        // webview.initialize(context)
        // webview.setCallback(this::callback)

        currentModule?.subtypes?.forEach { subtype ->
            currentModule.code?.get(subtype)?.mediaConsume?.forEach { watchFn ->
                // We need the info function to
                // be executed synchronously
                viewModelScope.launch {
                    syncLock.lock()
                    if (!webview.load(watchFn.code, decodedUrl)) {
                        return@launch
                    }
                }
            }
        }
    }
}
