package com.chouten.app.ui.views.HomePage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.chouten.app.data.ModuleModel

class HomePageViewModel() : ViewModel() {
    var selectedModule by mutableStateOf<ModuleModel?>(null)
        private set
    var availableModules by
    mutableStateOf(
        listOf(
            ModuleModel(
                "Zoro",
                "Inumaki",
                "1.0.0",
                "",
                "https://res.cloudinary.com/crunchbase-production/image/upload/c_lpad,f_auto,q_auto:eco,dpr_1/qe7kzhh0bo1qt9ohrxwb",
                false,
                "",
                0xFFFFCB3D,
                null
            ),
            ModuleModel(
                "GogoAnime",
                "Inumaki",
                "1.0.0",
                "",
                null,
                false,
                "",
                null,
                null
            )
        )
    )
        private set

    fun updateSelectedModule(module: ModuleModel) {
        println("Updating to ${module.name}")
        selectedModule = module
    }
}
