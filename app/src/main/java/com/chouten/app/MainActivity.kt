package com.chouten.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.chouten.app.ui.theme.ChoutenTheme
import com.chouten.app.ui.views.homePage.HomePage
import com.chouten.app.ui.views.homePage.HomePageViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initializeNetwork(applicationContext)
    setContent {
      ChoutenTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          HomePage(HomePageViewModel())
        }
      }
    }
  }
}
