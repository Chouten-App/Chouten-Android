package com.chouten.app.ui.views.home

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.launch

class HomePageViewModel(
    context: Context,
    private val webview: WebviewHandler = WebviewHandler()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _homeResults = mutableStateListOf<HomeResult>()
    val homeResults: List<HomeResult>
        get() = _homeResults

    private val _loadedModule = mutableStateOf<ModuleModel?>(null)
    val loadedModule: ModuleModel?
        get() = _loadedModule.value

    init {
        webview.initialize(context)
    }

    private val handler = Handler(Looper.getMainLooper())

    fun initialize() {
        viewModelScope.launch {
            loadHomePage()
        }
    }
    private suspend fun loadHomePage() {
        _loadedModule.value = ModuleLayer.selectedModule
        val homeModule = ModuleLayer.selectedModule ?: return
        isLoading = true
        homeModule.subtypes.forEach { subtype ->
            // wait for homeModule code to be loaded asynchronously using run handler
            handler.postDelayed(object : Runnable {
                override fun run() {
                    println("Checking if homeModule code is loaded...")
                    if (homeModule.code?.get(subtype)?.home?.get(0)?.code != null) {
                        println("HomeModule code is loaded!")
                        val homeFns = homeModule.code?.get(subtype)?.home
                        if (homeFns != null) {
                            viewModelScope.launch {
                                runHomeFns(homeFns)
                            }
                        }

                        handler.removeCallbacks(this)
                        return
                    }
                    handler.postDelayed(this, 100)
                }
            }, 100)

        }

    }

    private suspend fun runHomeFns(homeFns: List<ModuleModel.ModuleCode.ModuleCodeblock>) {
        homeFns.forEach { homeFn ->
            println("HOME FN: $homeFn")
            viewModelScope.launch {
                if (!webview.load(
                        homeFn,
                    )
                ) {
                    isLoading = false
                    return@launch
                }

                val res = webview.inject(homeFn)
                if (res.isBlank()) {
                    PrimaryDataLayer.enqueueSnackbar(
                        SnackbarVisualsWithError(
                            "Something went wrong while loading the home page.",
                            false
                        )
                    )
                    isLoading = false
                    return@launch
                }

                try {
                    val results =
                        Mapper.parse<ModuleResponse<List<HomeResult>>>(
                            res
                        )
                    println("RESULTS: $results")
                    _homeResults.clear()
                    _homeResults.addAll(results.result)
                    isLoading = false
                    webview.updateNextUrl(results.nextUrl)
                } catch (e: Exception) {
                    isLoading = false
                    PrimaryDataLayer.enqueueSnackbar(
                        SnackbarVisualsWithError(
                            e.localizedMessage ?: "Error parsing home page results.", isError = true
                        )
                    )
                    e.printStackTrace()
                }

            }

        }
    }

}
