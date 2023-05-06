package com.chouten.app.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chouten.app.ModuleLayer
import com.chouten.app.R
import com.chouten.app.ui.views.searchPage.ModuleChoice
import com.chouten.app.ui.views.searchPage.ModuleImportButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun ModuleSelectorContainer(
    context: Context,
    modifier: Modifier = Modifier,
    children: @Composable () -> Unit,
) {
    val sheetState = androidx.compose.material.rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )

    var importPopupState by rememberSaveable { mutableStateOf(false) }
    var importUrl by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val noModuleSelected = stringResource(R.string.no_module_selected)

    ModalBottomSheetLayout(
        modifier = modifier,
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
                    importPopupState = !importPopupState
                }
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    items(items = ModuleLayer.availableModules) { module ->
                        ModuleChoice(
                            module.id
                                ?: throw Exception("Module ID not set for ${module.name}"),
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
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
            3.dp
        ), //TODO: Replace with something else, cuz deprecated soon (elevation lv 2)
        sheetShape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(importPopupState) {
                AlertDialog(onDismissRequest = {
                    importPopupState = false
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
                                importPopupState = false
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
                            importPopupState = false
                        }) {
                            Text(stringResource(R.string.import_module_button_confirm))
                        }
                    })
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // We want the elevated button
                // to "float" above the rest of the content
                ElevatedButton(
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
                    shape = RoundedCornerShape(20),
                ) {
                    Text(
                        ModuleLayer.selectedModule?.name ?: noModuleSelected,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                children()
            }
        }
    }
}