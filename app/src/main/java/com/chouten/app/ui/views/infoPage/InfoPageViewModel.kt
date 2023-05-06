package com.chouten.app.ui.views.infoPage

import android.content.Context
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.Mapper
import com.chouten.app.ModuleLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.asyncMap
import com.chouten.app.data.InfoResult
import com.chouten.app.data.ModuleModel
import com.chouten.app.data.ModuleResponse
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.data.WebviewHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.net.URLDecoder

class InfoPageViewModel(
    context: Context,
    private var url: String,
    private var title: String
) : ViewModel() {

    private val webview = WebviewHandler()

    private val syncLock = Mutex(false)

    private var hasLoadedInfo by mutableStateOf(false)
    val hasLoadedInfoText: Boolean
        get() = hasLoadedInfo

    private var altTitles by mutableStateOf(listOf<String>())
    val altTitlesText: List<String>
        get() = altTitles

    private var description by mutableStateOf("")
    val descriptionText: String
        get() = description

    private var thumbnail by mutableStateOf("")
    val thumbnailUrl: String
        get() = thumbnail

    private var banner by mutableStateOf("")
    val bannerUrl: String
        get() = banner

    private var status by mutableStateOf("")
    val statusText: String
        get() = status

    private var mediaCount by mutableStateOf(0)
    val mediaCountText: Int
        get() = mediaCount

    private var mediaType by mutableStateOf("")
    val mediaTypeText: String
        get() = mediaType

    private var infoResult by mutableStateOf(listOf(listOf<InfoResult.MediaItem>()))
    val infoResults: List<List<InfoResult.MediaItem>>
        get() = infoResult

    init {
        // Both title and url are url-encoded.
        title = URLDecoder.decode(title, "UTF-8")
        val decodedUrl = URLDecoder.decode(url, "UTF-8")

        // We want to get the info code from the webview handler
        // and then load the page with that code.
        val currentModule = ModuleLayer.selectedModule
        webview.initialize(context)
        webview.updateNextUrl(decodedUrl)
        currentModule?.subtypes?.forEach { subtype ->
            val infoFns = currentModule.code[subtype]?.info
            infoFns?.forEach { infoFn ->
                // We need the info function to
                // be executed synchronously
                viewModelScope.launch {
                    syncLock.lock()
                    if (!webview.load(infoFn)) {
                        return@launch
                    }

                    val res = webview.inject(infoFn.javascript)
                    if (res.isBlank()) {
                        PrimaryDataLayer.enqueueSnackbar(
                            SnackbarVisualsWithError(
                                "No results found for $title",
                                false
                            )
                        )
                        return@launch
                    }

                    // use regex to convert things like \\" to "
                    try {
                        val json = res.substring(2..res.length - 3).replace(
                            "\\\"",
                            "\""
                        ).replace(
                            "\\\\",
                            "\\"
                        )
                        println("The JSON received was $json")
                        try {
                            val results =
                                Mapper.parse<ModuleResponse<InfoResult>>(json)
                            webview.updateNextUrl(results.nextUrl)
                            println("Results for info are ${results.result}")

                            val result = results.result
                            altTitles = result.altTitles
                            description = result.description
                            thumbnail = result.poster
                            banner = result.banner ?: ""
                            status = result.status ?: ""
                            mediaCount = result.totalMediaCount ?: 0
                            mediaType = result.mediaType
                            hasLoadedInfo = true
                        } catch (e: Exception) {
                            try {
                                val results = Mapper.parse<List<InfoResult.MediaItem>>(json)
                                infoResult = listOf(results)
                            } catch (e: Exception) {
                                PrimaryDataLayer.enqueueSnackbar(
                                    SnackbarVisualsWithError(
                                        "Error parsing results for $title",
                                        false
                                    )
                                )
                                throw Exception("Error parsing results for $title")
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        PrimaryDataLayer.enqueueSnackbar(
                            SnackbarVisualsWithError(
                                "Error parsing results for $title",
                                false
                            )
                        )
                        syncLock.unlock()
                        return@launch
                    }
                    syncLock.unlock()
                }
            }
        }
    }

    fun getTitle(): String {
        return title
    }

    fun getUrl(): String {
        return url
    }
}
