package com.chouten.app.ui.views.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.chouten.app.ModuleLayer
import com.chouten.app.data.HomeResult
import com.chouten.app.data.WebviewHandler
import com.chouten.app.ui.components.ModuleSelectorContainer
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Locale

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage(
    navController: NavController,
    provider: HomePageViewModel = HomePageViewModel(
        context = navController.context,
        WebviewHandler()
    ),
) {
    val scrollState = rememberScrollState()

    if (provider.homeResults.isEmpty() && ModuleLayer.selectedModule != null) {
        provider.viewModelScope.launch {
            provider.loadHomePage()
        }
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModuleSelectorContainer(context = navController.context) {
            AnimatedVisibility(
                provider.isLoading && ModuleLayer.selectedModule != null,
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

            // make a scrollable list of home items (carousel, grid, etc)
            Column(
                modifier = Modifier.verticalScroll(state = scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (result in provider.homeResults) {
                    when (result.type.lowercase(Locale.getDefault())) {
                        "carousel" -> {
                            val pagerState = rememberPagerState(0)

                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val maxHeight = maxHeight
                                val bottomHeight: Dp = maxHeight / 3
                                val topHeight: Dp = maxHeight * 2 / 3

                                val centerHeight = 200.dp
                                HorizontalPager(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                        .height(topHeight),
                                    pageCount = result.data.size,
                                    state = pagerState,

                                    ) {
                                    val item = result.data[it]
                                    GlideImage(
                                        imageModel = { item.image },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .clip(MaterialTheme.shapes.medium.copy(ZeroCornerSize)),
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

                                // add a modal on top of the carousel to show the current item, add blur to the background
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 10.dp, end = 10.dp, top = 200.dp)
                                        .shadow(
                                            elevation = 10.dp,
                                            shape = MaterialTheme.shapes.large
                                        )
                                        .wrapContentHeight(Alignment.CenterVertically)
                                        .align(Alignment.Center)
                                        .clip(MaterialTheme.shapes.large)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                1.dp
                                            )
                                        )
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 15.dp,
                                                    end = 10.dp,
                                                    top = 15.dp,
                                                    bottom = 5.dp
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // indicator
                                            result.data[pagerState.currentPage].indicator?.let {
                                                Text(
                                                    text = it,
                                                    modifier = Modifier
                                                        .padding(
                                                            5.dp
                                                        )
                                                        .background(
                                                            color = MaterialTheme.colorScheme.primary,
                                                            shape = RoundedCornerShape(20.dp)
                                                        )
                                                        .padding(
                                                            start = 10.dp,
                                                            end = 10.dp,
                                                            top = 2.dp,
                                                            bottom = 2.dp
                                                        ),
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontWeight = FontWeight.Bold,

                                                    )
                                            }

                                            result.data[pagerState.currentPage].iconText?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.8F
                                                        )
                                                    ),
                                                    modifier = Modifier
                                                        .padding(
                                                            end = 10.dp
                                                        ),
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                        }
                                        Text(
                                            result.data[pagerState.currentPage].titles["primary"]!!,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentWidth(Alignment.Start)
                                                .padding(start = 20.dp, end = 20.dp)
                                        )

                                        result.data[pagerState.currentPage].titles["secondary"]?.let {
                                            Text(
                                                it,
                                                // add some alpha to the color to make it grey
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.5F
                                                    )
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentWidth(Alignment.Start)
                                                    .padding(start = 20.dp, end = 20.dp),

                                                )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Row {
                                            result.data[pagerState.currentPage].subtitle?.let {
                                                Text(
                                                    it,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.8F
                                                        )
                                                    ),
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentWidth(Alignment.Start)
                                                        .padding(start = 20.dp, end = 20.dp)
                                                )
                                            }

                                            if (result.data[pagerState.currentPage].subtitleValue.isNotEmpty()) {
                                                Text(result.data[pagerState.currentPage].subtitleValue.joinToString(" * "))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            result.data[pagerState.currentPage].buttonText?.let {
                                                TextButton(
                                                    modifier = Modifier
                                                        .wrapContentWidth(Alignment.Start)
                                                        .padding(
                                                            start = 10.dp,
                                                            end = 20.dp,
                                                            bottom = 10.dp
                                                        ),
                                                    onClick = {
                                                        val currentItem =
                                                            result.data[pagerState.currentPage]
                                                        val title = URLEncoder.encode(
                                                            currentItem.titles["primary"],
                                                            "UTF-8"
                                                        )
                                                        val url = URLEncoder.encode(
                                                            currentItem.url,
                                                            "UTF-8"
                                                        )
                                                        navController.navigate("info/$title/$url")
                                                    }
                                                ) {
                                                    Text(
                                                        it,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.9F
                                                            )
                                                        ),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier
                                                            .wrapContentWidth(Alignment.Start)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 20.dp, bottom = 20.dp)
                                                    .wrapContentWidth(Alignment.End)
                                                    .size(24.dp),
                                                onClick = { /*TODO*/ },
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Add,
                                                    contentDescription = "Add to list",
                                                    modifier = Modifier
                                                        .padding(0.dp)
                                                        .size(24.dp)
                                                )
                                            }
                                        }

                                    }
                                }
                            }

                        }
                        "list" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp)
                            ) {
                                Text(
                                    result.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentWidth(Alignment.Start)
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                items(result.data) { item ->
                                    HomeResultItem(
                                        modifier = Modifier
                                            .height(250.dp)
                                            .width(130.dp),
                                        item = item
                                    ) { title, url ->
                                        navController.navigate("info/$title/$url")
                                    }
                                }
                            }
                        }
                        "grid_2x" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp)
                            ) {
                                Text(
                                    result.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentWidth(Alignment.Start)
                                )
                            }

                            LazyHorizontalGrid(
                                rows = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp) // TODO: make this dynamic
                                    .padding(start = 10.dp),
                            ) {
                                items(result.data) { item ->
                                    HomeResultItem(
                                        modifier = Modifier
                                            .height(150.dp) //TODO: make this dynamic
                                            .width(120.dp),
                                        item = item
                                    ) { title, url ->
                                        navController.navigate("info/$title/$url")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun HomeResultItem(
    modifier: Modifier = Modifier,
    item: HomeResult.HomeItem,
    onClick: (title: String, url: String) -> Unit
) {
    Column(
        modifier
            .padding(0.dp, 6.dp)
            .clickable {
                val title = URLEncoder.encode(item.titles["primary"], "UTF-8")
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
            item.indicator?.let {
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
                imageModel = { item.image },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    contentDescription = "${item.titles["primary"]} Thumbnail",
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
            item.titles["primary"] ?: "Unknown",
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
                text = item.current?.toString() ?: "~",
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
                text = item.total?.toString() ?: "~",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
        }
    }
}