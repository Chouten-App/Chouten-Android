package com.chouten.app.ui.views.homePage

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.chouten.app.ModuleLayer
import com.chouten.app.R
import com.chouten.app.ui.theme.dashedBorder
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun HomePage(context: Context, /*provider: HomePageViewModel = viewModel(),*/ navController: NavHostController) {
    val sheetState = androidx.compose.material.rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )

    val importPopupState = remember { mutableStateOf(false) }
    var importUrl by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val noModuleSelected = stringResource(R.string.no_module_selected)

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
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
                ModuleImportButton {
                    importPopupState.value = !importPopupState.value
                }
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
                                    module.id ?: throw Exception("Module ID not set for ${module.name}"),
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
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), //TODO: Replace with something else, cuz deprecated soon (elevation lv 2)
        sheetShape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.heightIn(TextFieldDefaults.MinHeight)) {
                // For some reason we need the FQN
                androidx.compose.animation.AnimatedVisibility(ModuleLayer.selectedModule?.name != null) {
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
                                    5.dp
                                ), CircleShape
                            )
                            .animateEnterExit(),
                        ModuleLayer.selectedModule?.name
                    )
                }

                androidx.compose.animation.AnimatedVisibility(importPopupState.value) {
                    AlertDialog(onDismissRequest = {
                        importPopupState.value = false
                    },
                        title = { Text(stringResource(R.string.import_module_header)) },
                        text = {
                            OutlinedTextField(
                                value = importUrl,
                                onValueChange = { importUrl = it },
                                label = { Text(stringResource(R.string.import_module_desc)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    coroutineScope.launch {
                                        ModuleLayer.enqueueRemoteInstall(
                                            context,
                                            importUrl.text
                                        )
                                        importUrl = TextFieldValue("")
                                    }
                                    importPopupState.value = false
                                })
                            )
                        },
                        confirmButton = {
                            FilledTonalButton(colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ), onClick = {
                                coroutineScope.launch {
                                    ModuleLayer.enqueueRemoteInstall(
                                        context,
                                        importUrl.text
                                    )
                                    importUrl = TextFieldValue("")
                                }
                                importPopupState.value = false
                            }) {
                                Text(stringResource(R.string.import_module_button_confirm))
                            }
                        })
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ElevatedButton(
                    onClick = {
                        coroutineScope.launch {
                            if (sheetState.isVisible) {
                                sheetState.hide()
                            } else sheetState.show()
                        }
                    },
                    shape = RoundedCornerShape(20),
                ) {
                    Text(
                        ModuleLayer.selectedModule?.name ?: noModuleSelected,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
) {
    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    val searchWith = stringResource(R.string.search_bar_with)
    val searchFallback = stringResource(
        R.string.search_bar_fallback
    )

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
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
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
    }
}
