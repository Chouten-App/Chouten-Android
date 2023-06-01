package com.chouten.app.ui.views.search

import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

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
            searchJob = viewModelScope.launch {
                _searchQuery.debounce(500).distinctUntilChanged()
                    .collectLatest {
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
        val searchModule = ModuleLayer.selectedModule ?: return
        searchModule.subtypes.forEach { subtype ->
            val searchFns = searchModule.code?.get(subtype)?.search

            searchFns?.forEach { searchFn ->
                // We need the search function to
                // be executed synchronously.
                viewModelScope.launch {
                    if (!webview.load(
                            searchFn.copy(
                                request = searchFn.request?.url?.let {
                                    searchFn.request.copy(
                                        url = it.replace(
                                            "<query>",
                                            withContext(Dispatchers.IO) {
                                                URLEncoder.encode(query, "UTF-8")
                                            }
                                        )
                                    )
                                }
                            )
                        )
                    ) {
                        isSearching = false
                        return@launch
                    }

                    val res = webview.inject(searchFn)
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

                    _searchResults.clear()
                    try {
                        val results =
                            Mapper.parse<ModuleResponse<List<SearchResult>>>(
                                res
                            )
                        _searchResults.addAll(results.result)
                        isSearching = false
                        webview.updateNextUrl(results.nextUrl)
                    } catch (e: Exception) {
                        PrimaryDataLayer.enqueueSnackbar(
                            SnackbarVisualsWithError(
                                "Error parsing search results for $query", true
                            )
                        )
                        e.printStackTrace()
                        e.localizedMessage?.let {
                            LogEntry(
                                title = "Error parsing search results for $query",
                                message = it,
                                isError = true,
                            )
                        }?.let {
                            LogLayer.addLogEntry(
                                it
                            )
                        }
                        isSearching = false
                    }
                }
            }
        }
    }
}
