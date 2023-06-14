package com.chouten.app.ui.views.info

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.chouten.app.Mapper
import com.chouten.app.ui.components.EpisodeItem
import com.chouten.app.ui.components.ShimmerEpisodes
import com.chouten.app.ui.components.ShimmerInfo
import com.chouten.app.ui.views.watch.PlayerActivity
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.serialization.json.encodeToJsonElement

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPage(
    provider: InfoPageViewModel,
    navController: NavController,
) {
    val scrollState = rememberScrollState()
    val gradient = Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        0.6f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    )

    var isDescriptionBoxExpanded by rememberSaveable { mutableStateOf(false) }
    var descriptionLineCount by rememberSaveable { mutableIntStateOf(0) }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    // Parallax effect for the banner
    val leftOffset =
        -(LocalConfiguration.current.screenWidthDp * 2) + (scrollState.value * 1.1).toInt()
    val offsetX by animateIntOffsetAsState(
        targetValue = if (scrollState.value >= 700) IntOffset(
            scrollState.value * 2,
            0
        ) else IntOffset.Zero,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), label = "offsetX"
    )
    val topBarOffset by animateIntOffsetAsState(
        targetValue = if (leftOffset >= 0) IntOffset.Zero else if (scrollState.value >= 700) IntOffset(
            leftOffset,
            0
        ) else IntOffset(-(LocalConfiguration.current.screenWidthDp * 4), 0),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), label = "topBarOffset"
    )

    ShimmerInfo(
        modifier = Modifier
            .navigationBarsPadding(),
        isLoading = !provider.hasLoadedInfoText,
        contentAfterLoading = {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .offset {
                                offsetX
                            }
                    ) {
                        GlideImage(
                            modifier = Modifier.blur((provider.bannerUrl.ifBlank { 2.dp } as Dp)),
                            imageModel = {
                                provider.bannerUrl.ifBlank { provider.thumbnailUrl }
                            },
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                                contentDescription = "${provider.getTitle()} Thumbnail",
                            ),
                        )

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(gradient)
                        )
                    }
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            Modifier
                                .padding(top = 180.dp, bottom = 8.dp)
                                .offset {
                                    offsetX
                                },
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlideImage(modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(MaterialTheme.shapes.medium),
                                imageModel = {
                                    provider.thumbnailUrl
                                },
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    contentDescription = "${provider.getTitle()} Thumbnail",
                                ),
                                loading = {
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )
                                },
                                failure = {
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )
                                })
                            Column(
                                Modifier.align(Alignment.Bottom),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Text(
                                    provider.altTitlesText.firstOrNull() ?: "",
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        0.7F
                                    ),
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    provider.getTitle(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(
                                        top = 12.dp, bottom = 8.dp
                                    )
                                ) {
                                    Text(
                                        provider.statusText,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${provider.mediaCountText} ${provider.mediaTypeText}",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } // top info

                        Text(provider.descriptionText,
                            Modifier
                                .clickable(
                                    enabled = true,
                                    onClickLabel = "Expand Description",
                                    role = Role.Switch
                                ) {
                                    isDescriptionBoxExpanded = !isDescriptionBoxExpanded
                                }
                                .animateContentSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7F),
                            fontSize = 15.sp,
                            lineHeight = 16.sp,
                            maxLines = if (!isDescriptionBoxExpanded) 9 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { textLayoutResult ->
                                descriptionLineCount = textLayoutResult.lineCount
                            }
                        )

                        AnimatedVisibility(
                            visible = descriptionLineCount >= 9,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    isDescriptionBoxExpanded = !isDescriptionBoxExpanded
                                },
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.End)
                            ) {
                                Text(
                                    if (!isDescriptionBoxExpanded) "Show More" else "Show Less",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                )
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(checked = false, onCheckedChange = {})
                            Text(
                                "Subbed",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    vertical = 6.dp, horizontal = 12.dp
                                )
                            )
                        }
                        Text(
                            provider.mediaTypeText,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        // TODO: dynamic maxheight based on screen
                        ShimmerEpisodes(isLoading = !provider.hasLoadedEpisodes && provider.hasLoadedInfoText,
                            contentAfterLoading = {
                                LazyColumn(
                                    Modifier.heightIn(max = 600.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    itemsIndexed(items = provider.infoResults[0].list) { _, item ->
                                        EpisodeItem(
                                            item,
                                            provider.thumbnailUrl,
                                            Modifier.clickable {
                                                if (provider.mediaTypeText.lowercase() == "episodes") {
                                                    val intent = Intent(
                                                        navController.context,
                                                        PlayerActivity::class.java
                                                    )
                                                    intent.putExtra(
                                                        "title",
                                                        provider.getTitle()
                                                    )
                                                    intent.putExtra("episode", item.title)
                                                    intent.putExtra("url", item.url)
                                                    intent.putExtra(
                                                        "episodeNumber",
                                                        item.number
                                                    )
                                                    // need a way to pass the list of episodes to the player
                                                    intent.putExtra(
                                                        "episodes",
                                                        provider.infoResults.map {
                                                            it.list.map { episode ->
                                                                episode.toString()
                                                            }
                                                        }.toString()
                                                    )

                                                    intent.putExtra("currentEpisodeIndex", provider.infoResults[0].list.indexOf(item))
                                                    ContextCompat.startActivity(
                                                        navController.context,
                                                        intent,
                                                        null
                                                    )
                                                    //navController.navigate("watch/${titleEncoded}/${episodeEncoded}/${urlEncoded}")
                                                } else {
                                                    throw Error("Reading not yet implemented")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
    )

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .offset {
                topBarOffset
            },
        title = {
            Text(
                provider.getTitle(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    vertical = 6.dp, horizontal = 12.dp
                )
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        // This action is used to add padding for the title (Not clickable). It's a hacky solution but it works
        actions = {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(
                        CircleShape
                    )
                    .size(24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) { }
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp),
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Back",
                    tint = Color.Transparent,
                )
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize().padding(top = systemBarsPadding.calculateTopPadding() * 2, end = 16.dp), contentAlignment = Alignment.TopEnd) {
        IconButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .clip(
                    CircleShape
                )
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Icon(
                modifier = Modifier
                    .padding(0.dp)
                    .size(20.dp),
                imageVector = Icons.Rounded.Close,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}