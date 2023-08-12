package com.chouten.app.ui.views.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.Mapper
import com.chouten.app.ModuleLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.HomeResult
import com.chouten.app.data.ModuleModel
import com.chouten.app.data.ModuleResponse
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.data.WebviewHandler
import com.chouten.app.data.ErrorAction
import com.chouten.app.data.ModuleAction
import kotlinx.coroutines.launch
import com.chouten.app.data.HomepagePayload

class HomePageViewModel(context: Context, private val webview: WebviewHandler = WebviewHandler()) :
        ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _homeResults = mutableStateListOf<HomeResult>()
    val homeResults: List<HomeResult>
        get() = _homeResults

    private val _loadedModule = mutableStateOf<ModuleModel?>(null)
    val loadedModule: ModuleModel?
        get() = _loadedModule.value

    var errors = 0

    init {
        webview.initialize(context)
        webview.dontCloseOnError()
        webview.setCallback(this::callback)
    }

    // This is called by the webview
    // when it's done calling the logic function
    fun callback(message: String) {

        val action = Mapper.parse<ModuleAction>(message).action

        try {
            if(action == "error"){
                val error = Mapper.parse<ErrorAction>(message)
                throw Exception(error.result)
            }

            val results = Mapper.parse<ModuleResponse<List<HomeResult>>>(message)
            
            _homeResults.clear()
            _homeResults.addAll(results.result)
        } catch (e: Exception) {
            errors += 1
            PrimaryDataLayer.enqueueSnackbar(
                    SnackbarVisualsWithError(
                            e.localizedMessage ?: "Error parsing home page results.",
                            isError = true
                    )
            )
            e.printStackTrace()
        }

        isLoading = false
    }

    private val handler = Handler(Looper.getMainLooper())

    // TODO: unify with init
    fun initialize(shouldReset: Boolean = false) {
        if(shouldReset){
            errors = 0
        }

        _homeResults.clear()
        viewModelScope.launch { loadHomePage() }
    }

    fun getCode(): String{
        val currentModule = ModuleLayer.selectedModule ?: throw Exception("No module selected")
        val subtype = currentModule.subtypes.getOrNull(0) ?: throw Exception("Subtype not found")
        return currentModule.code?.get(subtype)?.home?.getOrNull(0)?.code ?: throw Exception("Code not found")
    }

    private suspend fun loadHomePage() {
        _loadedModule.value = ModuleLayer.selectedModule
        
        isLoading = true
        val code = this.getCode()

        val webviewPayload = HomepagePayload(
            action = "homepage"
        )

        webview.load(
            code, 
            Mapper.json.encodeToString(HomepagePayload.serializer(), webviewPayload)
        )
        
    }
}
