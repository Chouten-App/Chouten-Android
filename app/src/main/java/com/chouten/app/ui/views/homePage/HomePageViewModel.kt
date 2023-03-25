package com.chouten.app.ui.views.homePage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.chouten.app.client
import com.chouten.app.data.ModuleModel
import kotlinx.coroutines.runBlocking

class HomePageViewModel() : ViewModel() {
    var selectedModule by mutableStateOf<ModuleModel?>(null)
        private set
    val res = runBlocking {
        client.get("https://gist.githubusercontent.com/TobyBridle/4ccaab3069db65e27fc28c4cbf65c749/raw/f21f7c639b6f62868c7165d17f62d03eac458bc9/test_module.json")
    }.parsed<ModuleModel>()
    var availableModules = listOf(res)
        private set

    fun updateSelectedModule(module: ModuleModel) {
        println("Updating to ${module.name}")
        selectedModule = module
    }
}
