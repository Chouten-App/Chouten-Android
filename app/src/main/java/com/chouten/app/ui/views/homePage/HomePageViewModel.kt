package com.chouten.app.ui.views.homePage

import androidx.lifecycle.ViewModel
import com.chouten.app.ModuleLayer

class HomePageViewModel() : ViewModel() {

  fun import(moduleUrl: String) {
    ModuleLayer.enqueueRemoteInstall(moduleUrl)
  }
}
