package com.chouten.app.ui.views.homePage

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chouten.app.data.SearchResult
import com.chouten.app.data.WebviewHandler
import com.chouten.app.ui.components.ModuleSelectorContainer
import com.chouten.app.ui.views.searchPage.SearchPageViewModel
import com.chouten.app.ui.views.searchPage.SearchResultItem

@Composable
fun HomePage(
    context: Context,
    provider: HomePageViewModel = HomePageViewModel(
        context = context,
        WebviewHandler()
    )
) {
    provider.initialize()
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModuleSelectorContainer(context = context) {
            AnimatedVisibility(
                provider.isLoading,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .zIndex(2F),
                enter = slideInVertically {
                    -it
                } + fadeIn(),
                exit = ExitTransition.None
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            LazyVerticalGrid(columns = GridCells.Adaptive(120.dp)) {
                items(items = provider.homeResults) {
                    SearchResultItem(
                        item = SearchResult(
                            title = "Lorem Ipsum",
                            url = "https://www.google.com",
                            img = "https://developers.elementor.com/docs/assets/img/elementor-placeholder-image.png",
                            indicatorText = null,
                            currentCount = null,
                            totalCount = null
                        ),
                        onClick = { _, _ -> }
                    )
                }
            }
        }
    }
}