package com.chouten.app.ui.views.infoPage

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.valentinilk.shimmer.shimmer

@Composable
fun InfoPage(
    provider: InfoPageViewModel
) {
    val scrollState = rememberScrollState()
    val gradient = Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        0.6f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    )

    var isDescriptionBoxExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {

        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                GlideImage(
                    modifier = Modifier.blur((provider.bannerUrl.ifBlank { 6.dp } as Dp)),
                    imageModel = {
                        provider.bannerUrl.ifBlank { provider.thumbnailUrl }
                    },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        contentDescription = "${provider.getTitle()} Thumbnail",
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
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(gradient)
                )
            } // Thumbnail
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    Modifier.padding(top = 180.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlideImage(modifier = Modifier
                        .width(120.dp)
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium),
                        imageModel = { provider.thumbnailUrl },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            contentDescription = "${provider.getTitle()} Thumbnail",
                        ),
                        loading = {
                            Box(
                                Modifier
                                    .shimmer()
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
                            isDescriptionBoxExpanded =
                                !isDescriptionBoxExpanded
                        }
                        .animateContentSize(),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7F),
                    fontSize = 15.sp,
                    lineHeight = 16.sp,
                    maxLines = if (!isDescriptionBoxExpanded) 9 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis)


                TextButton(
                    onClick = {
                        isDescriptionBoxExpanded = !isDescriptionBoxExpanded
                    },
                ) {
                    Text(
                        if (!isDescriptionBoxExpanded) "Show More" else "Show Less",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
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
                LazyColumn(
                    Modifier.heightIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(items = provider.infoResults[0]) { item ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        3.dp
                                    )
                                )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(
                                    12.dp
                                )
                            ) {
                                GlideImage(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(90.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    imageModel = {
                                        item.image ?: provider.thumbnailUrl
                                    },
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
                                Column(Modifier.heightIn(max = 90.dp)) {
                                    Column(
                                        modifier = Modifier.fillMaxHeight(
                                            0.7F
                                        ),
                                        verticalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Text(
                                            item.title ?: "Episode 1",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                bottom = 6.dp,
                                                end = 6.dp
                                            )
                                    ) {
                                        Text(
                                            "Episode ${
                                                item.number.toString()
                                                    .substringBefore(".0")
                                            }",
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                0.7F
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text(
                                            "24 mins",
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                0.7F
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            if (item.description?.isNotBlank() == true) {
                                Text(
                                    "${item.description}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        0.7F
                                    ),
                                    fontSize = 12.sp,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(all = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}