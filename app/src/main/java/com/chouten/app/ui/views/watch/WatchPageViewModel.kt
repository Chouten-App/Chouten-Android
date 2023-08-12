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


    init {
      
    }
}
