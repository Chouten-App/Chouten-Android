package com.chouten.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chouten.app.data.InfoResult
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.valentinilk.shimmer.shimmer

@Composable
fun EpisodeItem(
    item: InfoResult.MediaItem,
    imageAlternative: String,
    modifier: Modifier = Modifier
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    3.dp
                )
            )
            .then(modifier)
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
                    item.image ?: imageAlternative
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
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 6.dp, end = 6.dp
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