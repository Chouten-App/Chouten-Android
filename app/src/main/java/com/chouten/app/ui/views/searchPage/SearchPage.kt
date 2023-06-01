package com.chouten.app.ui.views.searchPage

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.chouten.app.ModuleLayer
import com.chouten.app.R
import com.chouten.app.data.SearchResult
import com.chouten.app.data.WebviewHandler
import com.chouten.app.ui.components.ModuleSelectorContainer
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.valentinilk.shimmer.shimmer
import java.net.URLEncoder

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchPage(
    navController: NavController,
    provider: SearchPageViewModel = SearchPageViewModel(
        navController.context,
        WebviewHandler()
    )
) {
    val lazygridScroll = rememberLazyGridState()
    Box {
        AnimatedVisibility(
            ModuleLayer.selectedModule?.name != null,
            modifier = Modifier
                .heightIn(TextFieldDefaults.MinHeight)
                .zIndex(2F)
        ) {
            ContentSearchBar(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 24.dp,
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            6.dp
                        ), CircleShape
                    ),
                ModuleLayer.selectedModule?.name,
                provider
            )
        }

        ModuleSelectorContainer(
            context = navController.context,
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedVisibility(
                provider.isSearching,
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

            LazyVerticalGrid(
                modifier = Modifier
                    .zIndex(1F)
                    .padding(top = TextFieldDefaults.MinHeight + 14.dp),
                columns = GridCells.Adaptive(100.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Center,
                state = lazygridScroll
            ) {
                items(items = provider.searchResults) { res ->
                    SearchResultItem(
                        item = res,
                        onClick = { title, url ->
                            println("Clicked on $title with url $url")
                            navController.navigate("info/$title/$url")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSearchBar(
    modifier: Modifier,
    activeModuleName: String?,
    provider: SearchPageViewModel
) {

    val searchWith = stringResource(R.string.search_bar_with)
    val searchFallback = stringResource(
        R.string.search_bar_fallback
    )

    val focusManager = LocalFocusManager.current

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        // We need to store our own copy of the search query, because the value
        // in the ViewModel is updated asynchronously
        var searchQuery by rememberSaveable { mutableStateOf(provider.searchQuery) }

        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it; provider.searchQuery = searchQuery
            },
            placeholder = {
                Text(
                    if (activeModuleName != null)
                        "$searchWith $activeModuleName"
                    else searchFallback
                )
            },
            leadingIcon = {
                IconButton(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .requiredWidth(IntrinsicSize.Max), onClick = {}) {
                    Icon(
                        Icons.Default.Search,
                        stringResource(R.string.search_bar_default),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .requiredWidth(IntrinsicSize.Max), onClick = {}) {
                    Icon(
                        Icons.Default.AccountCircle,
                        stringResource(R.string.active_profile),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // Close keyboard
                    focusManager.clearFocus()
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier,
    item: SearchResult,
    onClick: (title: String, url: String) -> Unit
) {
    Column(
        modifier
            .padding(0.dp, 6.dp)
            .clickable {
                val title = URLEncoder.encode(item.title, "UTF-8")
                val url = URLEncoder.encode(item.url, "UTF-8")
                println("WE RUN THE CLICAKBLE TIHNGYMAGIGY")
                onClick(title, url)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
                .heightIn(100.dp, 160.dp)
                .clip(MaterialTheme.shapes.small)
        ) {
            item.indicatorText?.let {
                if (it.isBlank()) return@let
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.shapes.small.copy(
                                topStart = ZeroCornerSize,
                                topEnd = ZeroCornerSize,
                                bottomEnd = ZeroCornerSize
                            )
                        )
                        .padding(8.dp, 4.dp)
                        .zIndex(2F)
                )
            }
            GlideImage(
                modifier = Modifier.zIndex(1F),
                imageModel = { item.img },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    contentDescription = "${item.title} Thumbnail",
                ),
                loading = {
                    Box(
                        Modifier
                            .shimmer()
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                },
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            item.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth(0.9F)
                .wrapContentWidth(Alignment.Start)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
        ) {
            Text(
                text = item.currentCount?.toString() ?: "~",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
            Text(
                " | ",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
            Text(
                text = item.totalCount?.toString() ?: "~",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
        }
    }
}