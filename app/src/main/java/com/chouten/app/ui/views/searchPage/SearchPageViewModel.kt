package com.chouten.app.ui.views.searchPage

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.ModuleLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.data.ModuleResponse
import com.chouten.app.data.SearchResult
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.data.WebviewHandler
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SearchPageViewModel(context: Context, private val webview: WebviewHandler = WebviewHandler()) : ViewModel() {
    var isSearching by mutableStateOf(false)
        private set
    var searchQuery by mutableStateOf("")
    var previousSearchQuery by mutableStateOf("")
        private set // We don't want to be able to set this from outside the class.

    init {
        webview.initialize(context)
    }

    private val _searchResults = mutableStateListOf<SearchResult>()
    val searchResults: List<SearchResult>
        get() = _searchResults

    fun search(query: String) {
        previousSearchQuery = searchQuery
        // We want to get the currently selected module and then
        // search for the query within that module.
        isSearching = true
        val searchModule = ModuleLayer.selectedModule ?: return
        searchModule.subtypes.forEach { subtype ->
            val searchFns = searchModule.code[subtype]?.search
            searchFns?.forEach { searchFn ->
                // We need the search function to
                // be executed synchronously.
                viewModelScope.launch {
                    if (!webview.load(
                        searchFn.request.copy(
                            url = searchFn.request.url.replace(
                                "<query>", query
                            )
                        )
                    )) {
                        isSearching = false
                        return@launch
                    }

                    val res = webview.inject(searchFn.javascript)
                    if (res.isBlank()) {
                        PrimaryDataLayer.enqueueSnackbar(
                            SnackbarVisualsWithError(
                                "No results found for $query",
                                false
                            )
                        )
                        isSearching = false
                        return@launch
                    }

                    val json =
                        res.substring(2..res.length - 3).replace("\\\"", "\"")
                    _searchResults.clear()
                    val results = Json.decodeFromString<ModuleResponse<List<SearchResult>>>(json)
                    _searchResults.addAll(results.data)
                    isSearching = false
                    webview.updateNextUrl(results.nextUrl)
                }
            }
        }
    }
}
