package com.chouten.app.ui.views.homePage

import android.content.Context
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.chouten.app.data.HomeResult
import com.chouten.app.data.SearchResult
import com.chouten.app.data.WebviewHandler
import com.chouten.app.ui.components.ModuleSelectorContainer
import com.chouten.app.ui.views.searchPage.SearchPageViewModel
import com.chouten.app.ui.views.searchPage.SearchResultItem
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.valentinilk.shimmer.shimmer
import java.net.URLEncoder
import java.util.Locale

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
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModuleSelectorContainer(context = navController.context) {
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
                                        .wrapContentHeight(Alignment.CenterVertically)
                                        .align(Alignment.Center)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .shadow(3.dp)
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

                                            Text(
                                                text = result.title,
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

                                            //TODO: add release date to far right if it exists
                                            // Text(...)
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
                                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F)),
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

                                        // TODO: Maybe add the description somewhere here

                                        result.data[pagerState.currentPage].iconText?.let {
                                            Text(
                                                it,
                                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8F)),
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentWidth(Alignment.Start)
                                                    .padding(start = 20.dp, end = 20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                                            result.data[pagerState.currentPage].buttonText?.let {
                                                TextButton(
                                                    modifier = Modifier
                                                        .wrapContentWidth(Alignment.Start)
                                                        .padding(
                                                            start = 10.dp,
                                                            end = 20.dp,
                                                            bottom = 10.dp
                                                        ),
                                                    onClick = { /*TODO*/ }
                                                ) {
                                                    Text(
                                                        it,
                                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9F)),
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
                                                modifier = Modifier.fillMaxWidth().padding(end = 20.dp, bottom = 20.dp)
                                                    .wrapContentWidth(Alignment.End).size(24.dp),
                                                onClick = { /*TODO*/ },
                                                colors =  IconButtonDefaults.iconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Add,
                                                    contentDescription = "Add to list",
                                                    modifier = Modifier.padding(0.dp).size(24.dp)
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
                        // TODO: other types
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