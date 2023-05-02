package com.chouten.app.ui.views.searchPage

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.chouten.app.ModuleLayer
import com.chouten.app.R
import com.chouten.app.data.SearchResult
import com.chouten.app.data.WebviewHandler
import com.chouten.app.ui.components.ModuleSelectorContainer
import com.chouten.app.ui.theme.dashedBorder
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.valentinilk.shimmer.shimmer
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchPage(
    context: Context,
    provider: SearchPageViewModel = SearchPageViewModel(
        context,
        WebviewHandler()
    )
) {
    val lazygridScroll = rememberLazyGridState()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            ModuleLayer.selectedModule?.name != null,
            modifier = Modifier.heightIn(TextFieldDefaults.MinHeight)
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
                    )
//                .animateEnterExit(),
                ,
                ModuleLayer.selectedModule?.name,
                provider
            )
        }

        ModuleSelectorContainer(
            context = context,
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedVisibility(
                provider.isSearching
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            // We want a gradient from transparent to surfaceColorAtElevation
                            // to make it look like the search bar is fading into the background
                            Brush.verticalGradient(
                                0F to MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    0.dp
                                ),
                                0.5F to MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    3.dp
                                ),
                                1F to MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    3.dp
                                ),
                            )
                        )
                        .zIndex(2F)
                ) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.Center)
                    )
                }
            }

            LazyVerticalGrid(
                modifier = Modifier.zIndex(1F),
                columns = GridCells.Adaptive(100.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Center,
                state = lazygridScroll
            ) {
                items(items = provider.searchResults) { res ->
                    SearchResultItem(
                        item = res
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleChoice(
    id: Int,
    name: String,
    author: String,
    version: String,
    icon: String?,
    backgroundColor: Color?,
    foregroundColor: Color?,
    onClick: () -> Unit
) {
    val iconSize = 40
    val iconSizePx: Int = iconSize * LocalDensity.current.density.roundToInt()

    Button(
        modifier = Modifier
            .fillMaxWidth(1F)
            .height(65.dp)
            .padding(vertical = 4.dp),
        colors = if (backgroundColor != null) {
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = foregroundColor
                    ?: MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else ButtonDefaults.buttonColors(),
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        content = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon == null) Icon(
                    Icons.Default.Help,
                    "Question Mark",
                    modifier = Modifier.size(iconSize.dp)
                )
                else GlideImage(
                    imageModel = { icon },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center,
                        contentDescription = "Favicon for the $name module",
                        requestSize = IntSize(iconSizePx, iconSizePx)
                    ),
                    loading = {
                        Box(Modifier.matchParentSize()) {
                            CircularProgressIndicator(
                                Modifier.align(
                                    Alignment.Center
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .size(iconSize.dp)
                        .clip(CircleShape),
                )

                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        name,
                        fontWeight = FontWeight.Bold,
                        color = foregroundColor
                            ?: MaterialTheme.colorScheme.onSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.Start) {
                        Text(
                            author,
                            color = foregroundColor?.copy(0.8F)
                                ?: MaterialTheme.colorScheme.onSecondary,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "v$version",
                            color = foregroundColor?.copy(0.8F)
                                ?: MaterialTheme.colorScheme.onSecondary,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun ModuleImportButton(onClick: () -> Unit) {
    val iconSize = 25

    Button(modifier = Modifier
        .fillMaxWidth(1F)
        .height(65.dp)
        .padding(vertical = 4.dp)
        .dashedBorder(
            1.dp, MaterialTheme.colorScheme.onPrimaryContainer, 10.dp
        ), colors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        //                contentColor = foregroundColor
    ), shape = RoundedCornerShape(10.dp), onClick = onClick, content = {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Download,
                stringResource(R.string.module_selection_header),
                modifier = Modifier
                    .size(iconSize.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    stringResource(R.string.import_module_header),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        stringResource(R.string.import_module_desc),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    })
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

        TextField(
            value = provider.searchQuery,
            onValueChange = { provider.searchQuery = it },
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
                        .padding(start = 16.dp)
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
                        .padding(end = 16.dp)
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
                    if (provider.searchQuery.isBlank() ||
                        provider.previousSearchQuery.trim() == provider.searchQuery.trim()
                    ) return@KeyboardActions
                    provider.search(provider.searchQuery)
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
) {
    Column(
        modifier.padding(0.dp, 6.dp),
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
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.onSurface)
                            .shimmer()
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