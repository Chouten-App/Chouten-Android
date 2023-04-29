package com.chouten.app.ui.views.searchPage

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.ModuleLayer
import com.chouten.app.data.ModuleResponse
import com.chouten.app.data.SearchResult
import com.chouten.app.data.WebviewHandler
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SearchPageViewModel(context: Context, private val webview: WebviewHandler = WebviewHandler()) : ViewModel() {

    init {
        webview.initialize(context)
    }

    private val _searchResults = mutableStateListOf<SearchResult>()
    val searchResults: List<SearchResult>
        get() = _searchResults

    fun search(query: String) {
        // We want to get the currently selected module and then
        // search for the query within that module.
        val searchModule = ModuleLayer.selectedModule ?: return
        searchModule.subtypes.forEach { subtype ->
            val searchFns = searchModule.code[subtype]?.search
            searchFns?.forEach { searchFn ->
                // We need the search function to
                // be executed synchronously.
                viewModelScope.launch {
                    webview.load(
                        searchFn.request.copy(
                            url = searchFn.request.url.replace(
                                "<query>", query
                            )
                        )
                    )
                    val res = webview.inject(searchFn.javascript)
                    val json =
                        res.substring(2..res.length - 3).replace("\\\"", "\"")
                    val results = Json.decodeFromString<ModuleResponse<List<SearchResult>>>(json)
                    webview.updateNextUrl(results.nextUrl)
                }
            }
        }
    }
}