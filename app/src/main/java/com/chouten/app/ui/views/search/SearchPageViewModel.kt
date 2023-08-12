package com.chouten.app.ui.views.search

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.LogLayer
import com.chouten.app.Mapper
import com.chouten.app.ModuleLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.LogEntry
import com.chouten.app.data.ModuleResponse
import com.chouten.app.data.SearchResult
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.data.WebviewHandler
import com.chouten.app.data.ModuleAction
import com.chouten.app.data.ErrorAction
import com.chouten.app.data.WebviewPayload
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SearchPageViewModel(
        context: Context,
        private val webview: WebviewHandler = WebviewHandler()
) : ViewModel() {
    var isSearching by mutableStateOf(false)
        private set

    // We want to keep track of the search job so that
    // we can cancel it if the search query changes.
    private var searchJob: Job? = null

    // We want to use a flow for this so that we
    // can have a debounce on the search.
    // Every time the search query changes, we want to
    // wait 500ms before actually searching.
    private var _searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    var searchQuery: String = _searchQuery.value
        set(value) {
            field = value
            _searchQuery.value = value
            searchJob?.cancel()
            searchJob =
                    viewModelScope.launch {
                        _searchQuery.debounce(500).distinctUntilChanged().collectLatest {
                            isSearching = true
                            if (it.isBlank()) {
                                _searchResults.clear()
                                isSearching = false
                                return@collectLatest
                            }
                            println("Searching for $it")
                            _searchQuery.value = it
                            search(it)
                        }
                    }
        }

    var previousSearchQuery by mutableStateOf("")
        private set // We don't want to be able to set this from outside the class.

    init {
        webview.initialize(context)
        webview.dontCloseOnError()
        webview.setCallback(this::callback)
    }

    fun getCode(): String{
        val currentModule = ModuleLayer.selectedModule ?: throw Exception("No module selected")
        val subtype = currentModule.subtypes.getOrNull(0) ?: throw Exception("Subtype not found")
        return currentModule.code?.get(subtype)?.search?.getOrNull(0)?.code ?: throw Exception("Code not found")
    }

    // This is called by the webview
    // when it's done calling the logic function
    fun callback(message: String) {

        if (message.isBlank()) {
            PrimaryDataLayer.enqueueSnackbar(
                    SnackbarVisualsWithError("No results found ", false)
            )
            isSearching = false
        }

        _searchResults.clear()

        val action = Mapper.parse<ModuleAction>(message).action

        try {
            if(action == "error"){
                val error = Mapper.parse<ErrorAction>(message)
                throw Exception(error.result)
            }
            
            val results = Mapper.parse<ModuleResponse<List<SearchResult>>>(message)
            _searchResults.addAll(results.result)
            isSearching = false
        } catch (e: Exception) {
            PrimaryDataLayer.enqueueSnackbar(
                    SnackbarVisualsWithError("Error parsing search results: " + e.message, true)
            )
            e.printStackTrace()
            e.localizedMessage
                    ?.let {
                        LogEntry(
                                title = "Error parsing search results",
                                message = it,
                                isError = true,
                        )
                    }
                    ?.let { LogLayer.addLogEntry(it) }
            isSearching = false
        }
    }

    private val _searchResults = mutableStateListOf<SearchResult>()
    val searchResults: List<SearchResult>
        get() = _searchResults

    private suspend fun search(query: String) {
        // Update the previous search query.
        // By setting it to the searchQuery value,
        // we can ensure that it is always up to date.
        previousSearchQuery = _searchQuery.value

        // We want to get the currently selected module and then
        // search for the query within that module.
        isSearching = true

        val code = getCode()

        if(!code.isEmpty()){
            val webviewPayload = WebviewPayload(
                                    query = query,
                                    action = "search"
                                )

            webview.load(code, Mapper.json.encodeToString(WebviewPayload.serializer(), webviewPayload))
        }
    }
}
