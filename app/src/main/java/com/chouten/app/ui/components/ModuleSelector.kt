package com.chouten.app.ui.components

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chouten.app.ModuleLayer
import com.chouten.app.PrimaryDataLayer
import com.chouten.app.R
import com.chouten.app.data.SnackbarVisualsWithError
import com.chouten.app.toBoolean
import com.chouten.app.ui.theme.dashedBorder
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun ModuleSelectorContainer(
    context: Context,
    modifier: Modifier = Modifier,
    children: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.Expanded },
        skipHalfExpanded = true,
    )

    var importPopupState by rememberSaveable { mutableStateOf(false) }
    var isAnimated by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val noModuleSelected = stringResource(R.string.no_module_selected)

    ModalBottomSheetLayout(
        modifier = modifier, sheetState = sheetState, sheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(15.dp, 0.dp, 15.dp, 25.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    BottomSheetDefaults.DragHandle()
                }
                Text(
                    stringResource(R.string.module_selection_header),
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    stringResource(R.string.module_selection_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(.8F)
                )
                Spacer(Modifier.height(20.dp))
                ModuleImportButton(onClick = {
                    importPopupState = !importPopupState
                    isAnimated = !isAnimated
                }, isAnimated = isAnimated)

                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    items(items = ModuleLayer.availableModules) { module ->
                        ModuleChoice(
                            module.id ?: throw Exception("Module ID not set for ${module.name}"),
                            module.name,
                            module.meta.author,
                            module.version,
                            module.meta.icon,
                            module.meta.backgroundColor.let {
                                val str = "FF" + it.removePrefix("#")
                                Color(str.toLong(16))
                            },
                            module.meta.foregroundColor.let {
                                val str = "FF" + it.removePrefix("#")
                                Color(str.toLong(16))
                            },
                            onClick = {
                                ModuleLayer.updateSelectedModule(
                                    module.id
                                        ?: throw Exception("Module ID not set for ${module.name}"),
                                )
                                coroutineScope.launch { sheetState.hide() }
                            },
                        )
                    }
                }
            }
            Divider(
                modifier = Modifier.padding(16.dp, 0.dp),
            )
        }, sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
            3.dp
        ), //TODO: Replace with something else, cuz deprecated soon (elevation lv 2)
        sheetShape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp)
    ) {
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Expand the module import button in height to show a textfield and cancle and confirm buttons

            if (importPopupState) {

            }

//                AnimatedVisibility(importPopupState) {
//
//                AlertDialog(onDismissRequest = {
//                    importPopupState = false
//                },
//                    title = { Text(stringResource(R.string.import_module_header)) },
//                    text = {
//                        OutlinedTextField(
//                            value = importUrl,
//                            onValueChange = { importUrl = it },
//                            label = { Text(stringResource(R.string.import_module_desc)) },
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = MaterialTheme.shapes.medium,
//                            singleLine = true,
//                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                            keyboardActions = KeyboardActions(onDone = {
//                                coroutineScope.launch {
//                                    ModuleLayer.enqueueRemoteInstall(
//                                        context,
//                                        importUrl.text
//                                    )
//                                    importUrl = TextFieldValue("")
//                                }
//                                importPopupState = false
//                            })
//                        )
//                    },
//                    confirmButton = {
//                        FilledTonalButton(colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary,
//                            contentColor = MaterialTheme.colorScheme.onPrimary,
//                        ), onClick = {
//                            coroutineScope.launch {
//                                ModuleLayer.enqueueRemoteInstall(
//                                    context,
//                                    importUrl.text
//                                )
//                                importUrl = TextFieldValue("")
//                            }
//                            importPopupState = false
//                        }) {
//                            Text(stringResource(R.string.import_module_button_confirm))
//                        }
//                    })
//            }

            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                // We want the elevated button
                // to "float" above the rest of the content
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .zIndex(Float.MAX_VALUE),
                    onClick = {
                        coroutineScope.launch {
                            if (sheetState.isVisible) {
                                sheetState.hide()
                            } else sheetState.show()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = ModuleLayer.selectedModule?.name ?: noModuleSelected,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                children()
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
                contentColor = foregroundColor ?: MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else ButtonDefaults.buttonColors(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        content = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon == null) Icon(
                    Icons.Default.Help, "Question Mark", modifier = Modifier.size(iconSize.dp)
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
                        color = foregroundColor ?: MaterialTheme.colorScheme.onSecondary,
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
fun ModuleImportButton(onClick: () -> Unit, isAnimated: Boolean = false) {
    val iconSize = 25
    val selectors = listOf("Module", "Theme")
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val expandedHeight = 0.65f
    val collapsedHeight = 65.dp
    val currentHeightF by animateFloatAsState(
        if (isAnimated) {
            expandedHeight
        } else {
            // We want collapsed height as a fraction of the screen height
            // so we can animate it later
            collapsedHeight.value / screenHeight.value
        }, animationSpec = tween(
            durationMillis = 1000, easing = FastOutSlowInEasing
        )
    )

    val currentHeight by animateDpAsState(
        if (isAnimated) {
            360.dp
        } else collapsedHeight, animationSpec = tween(
            durationMillis = 1000, easing = FastOutSlowInEasing
        )
    )

    var importType by remember { mutableStateOf(0) } // 0 = Module, 1 = Theme

    var importFromUrlText by remember { mutableStateOf(TextFieldValue("")) }
    var fileNameText by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(
                2.dp, MaterialTheme.colorScheme.onPrimaryContainer, 10.dp
            ),
    ) {
        if (!isAnimated) {
            Button(modifier = Modifier
                .fillMaxWidth()
                .height(currentHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                onClick = onClick,
                content = {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Rounded.Download,
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
        } else {
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                modifier = Modifier
                    .fillMaxHeight(currentHeightF)
                    .padding(15.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Row(horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource, indication = null
                        ) { onClick() }) {
                    Icon(
                        Icons.Rounded.Download,
                        stringResource(R.string.module_selection_header),
                        modifier = Modifier
                            .size(iconSize.dp)
                            .clip(CircleShape),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(if (!importType.toBoolean()) R.string.import_module_header else R.string.import_theme_header),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
//                        Column {
//                            Text(
//                                stringResource(R.string.import_module_header),
//                                color = MaterialTheme.colorScheme.onPrimaryContainer,
//                                fontWeight = FontWeight.Bold,
//                            )
//                            Spacer(modifier = Modifier.width(6.dp))
//                            Column {
//                                Text(
//                                    stringResource(R.string.import_module_desc),
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
//                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
//                                    fontWeight = FontWeight.SemiBold,
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis
//                                )
//                            }
//                        }
                }

                Text(
                    modifier = Modifier.padding(5.dp),
                    text = stringResource(if (importType.toBoolean()) R.string.import_module_description else R.string.import_theme_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7F),
                    overflow = TextOverflow.Ellipsis,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)

                ) {
                    SegmentedControl(
                        modifier = Modifier
                            .fillMaxWidth(),
                        items = selectors,
                        cornerRadius = 50,
                        itemWidth = 140.dp,
                        //useFixedWidth = true,
                        onItemSelection = {
                            importType = it
                        })
                    OutlinedTextField(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                        value = importFromUrlText,
                        maxLines = 1,
                        singleLine = true,
                        label = { Text(text = "Import from URL") },
                        onValueChange = {
                            importFromUrlText = it
                        })
                    OutlinedTextField(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                        value = fileNameText,
                        maxLines = 1,
                        singleLine = true,
                        label = { Text(text = "Filename") },
                        onValueChange = {
                            fileNameText = it
                        },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onClick,
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            if (importFromUrlText.text.isBlank() || fileNameText.text.isBlank()) {
                                PrimaryDataLayer.enqueueSnackbar(
                                    SnackbarVisualsWithError(
                                        "Import URL or Filename is empty!", true
                                    )
                                )
                            }
                            when (importType) {
                                0 -> {
                                    // TODO: import module from URL
                                }

                                1 -> {
                                    println("Importing theme from URL is not supported yet!")
                                }
                            }

                        },
                    ) {
                        Text(
                            stringResource(R.string.import_module),
                        )
                    }
                }

            }
        }
    }
}