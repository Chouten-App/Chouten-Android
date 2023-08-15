package com.chouten.app.ui.views.home

import android.content.Context
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

    init {
        webview.initialize(context)
        this.initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            loadHomePage()
        }
    }
    suspend fun loadHomePage() {
        val homeModule = ModuleLayer.selectedModule ?: return
        isLoading = true
        homeModule.subtypes.forEach { subtype ->
            val homeFns = homeModule.code?.get(subtype)?.home
            homeFns?.forEach { homeFn ->
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
                        println("RESULTS: "+results)
                        isLoading = false
                        _homeResults.addAll(results.result)
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

}