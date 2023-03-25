package com.chouten.app.ui.views.homePage

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.chouten.app.client
import com.chouten.app.data.ModuleModel

class HomePageViewModel() : ViewModel() {
  var selectedModule by mutableStateOf<ModuleModel?>(null)
    private set
  var availableModules = mutableStateListOf<ModuleModel>()
    private set

  fun updateSelectedModule(module: ModuleModel) {
    println("Updating to ${module.name}")
    selectedModule = module
  }

  suspend fun import(moduleUrl: String) {
    try {
      availableModules += client.get(moduleUrl).parsed<ModuleModel>()
    } catch (e: Exception) {
      Log.e("IMPORT", "Could not import $moduleUrl")
      e.localizedMessage?.let { Log.e("IMPORT", it) }
    }
  }
}
