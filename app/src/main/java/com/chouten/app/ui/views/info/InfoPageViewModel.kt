package com.chouten.app.ui.views.info

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.Mapper
import com.chouten.app.ModuleLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.InfoResult
import com.chouten.app.data.ModuleResponse
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.data.WebviewHandler
import com.chouten.app.data.ModuleAction
import com.chouten.app.data.ErrorAction
import java.net.URLDecoder
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.json.JSONObject
import kotlin.collections.getOrNull

class InfoPageViewModel(context: Context, private val url: String, private var title: String) :
        ViewModel() {

    private val webview = WebviewHandler()

    private val syncLock = Mutex(false)

    private var hasLoadedInfo by mutableStateOf(false)
    val hasLoadedInfoText: Boolean
        get() = hasLoadedInfo

    private var hasLoadedMediaEpisodes by mutableStateOf(false)
    val hasLoadedEpisodes: Boolean
        get() = hasLoadedMediaEpisodes

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

    private var mediaCount by mutableIntStateOf(0)
    val mediaCountText: Int
        get() = mediaCount

    private var mediaType by mutableStateOf("")
    val mediaTypeText: String
        get() = mediaType

    private var infoResult by mutableStateOf(listOf<InfoResult.MediaListItem>())
    val infoResults: List<InfoResult.MediaListItem>
        get() = infoResult

    fun getCode(): String{
        val currentModule = ModuleLayer.selectedModule;
        val subtype = currentModule?.subtypes?.getOrNull(0);
        var code: String;

        if(subtype == null){
            throw Exception("Subtype not found");
        }else{
            val tempCode = currentModule.code?.get(subtype)?.info?.getOrNull(0)?.code;

            if(tempCode == null){
                throw Exception("code not found");
            }else{
                code = tempCode;
            }
        }   
        return code;
    }

    fun callback(message: String) {
        val title = ""

        if (message.isBlank()) {
            PrimaryDataLayer.enqueueSnackbar(
                SnackbarVisualsWithError("No results found for $title", false)
            )
        }

        val action = Mapper.parse<ModuleAction>(message).action;

        try{
            if(action == "error"){
                val error = Mapper.parse<ErrorAction>(message);
                throw Exception(error.result);
            }

            when(action){
                "metadata" -> {
                    try {
                        val results = Mapper.parse<ModuleResponse<InfoResult>>(message)
        
                        val result = results.result
                        altTitles = result.altTitles!!
                        description = result.description
                        thumbnail = result.poster
                        banner = result.banner ?: ""
                        status = result.status ?: ""
                        mediaCount = result.totalMediaCount ?: 0
                        mediaType = result.mediaType
                        hasLoadedInfo = true
        
                        val epListURL = result.epListURLs[0];
                        val webviewPayload = mapOf<String, String>(
                            "query" to epListURL,
                            "action" to "eplist"
                        );
                        val code = this.getCode();
                        
                        viewModelScope.launch {
                            webview.load(code, JSONObject(webviewPayload).toString());
                        };
        
                    } catch (e: Exception) {
                        e.printStackTrace()
                        PrimaryDataLayer.enqueueSnackbar(
                                SnackbarVisualsWithError("Error parsing results for $title", false)
                        )
                    }
                }
                "eplist" -> {
                    try {
                        val results = Mapper.parse<ModuleResponse<List<InfoResult.MediaListItem>>>(message)
                        infoResult = results.result
                        hasLoadedMediaEpisodes = true
                    } catch (e: Exception) {
                        PrimaryDataLayer.enqueueSnackbar(
                                SnackbarVisualsWithError(
                                        "Error parsing second results for $title",
                                        false
                                )
                        )
                        println("Parsing error: $e")
                        throw Exception("Error parsing results for $title")
                    }
                }
                else -> {
                    throw Exception("Action not found. The action must be either set to 'eplist' or 'metadata'")
                }
            }
        }catch(e: Exception){
            PrimaryDataLayer.enqueueSnackbar(
                    SnackbarVisualsWithError(
                            e.localizedMessage ?: "Error parsing home page results.",
                            isError = true
                    )
            )
            e.printStackTrace()
        }
    }
    
    init {
        // Both title and url are url-encoded.
        title = URLDecoder.decode(title, "UTF-8")
        val decodedUrl = URLDecoder.decode(url, "UTF-8")
        
        // We want to get the info code from the webview handler
        // and then load the page with that code.
        webview.initialize(context)
        webview.setCallback(this::callback);

        val code = this.getCode();
        val webviewPayload = mapOf<String, String>(
            "query" to decodedUrl,
            "action" to "metadata"
        );
            
        viewModelScope.launch {
            webview.load(code, JSONObject(webviewPayload).toString());
        };

    }

    fun getTitle(): String {
        return title
    }

    fun getUrl(): String {
        return url
    }

    override fun onCleared() {
        webview.destroy()
        super.onCleared()
    }
}
