package com.chouten.app.ui.views.homePage

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chouten.app.data.SearchResult
import com.chouten.app.ui.components.ModuleSelectorContainer
import com.chouten.app.ui.views.searchPage.SearchResultItem

@Composable
fun HomePage(context: Context) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModuleSelectorContainer(context = context) {
            LazyVerticalGrid(columns = GridCells.Adaptive(120.dp)) {
                items(100) {
                    SearchResultItem(
                        item = SearchResult(
                            title = "Lorem Ipsum",
                            url = "https://www.google.com",
                            img = "https://developers.elementor.com/docs/assets/img/elementor-placeholder-image.png",
                            indicatorText = null,
                            currentCount = null,
                            totalCount = null
                        )
                    )
                }
            }
        }
    }
}