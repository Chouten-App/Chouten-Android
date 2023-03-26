package com.chouten.app.ui.views.homePage

import android.annotation.SuppressLint
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chouten.app.ModuleLayer
import com.chouten.app.R
import com.chouten.app.data.ModuleModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class)
@Composable
fun HomePage(provider: HomePageViewModel = viewModel()) {
  val sheetState =
      androidx.compose.material.rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Hidden,
      )

  val importPopupState = remember { mutableStateOf(false) }
  var importUrl by
      rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

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
          Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            BottomSheetDefaults.DragHandle()
          }
          Text(
              stringResource(R.string.module_selection_header),
              fontSize = MaterialTheme.typography.titleLarge.fontSize,
              fontWeight = FontWeight.Bold)
          Spacer(Modifier.height(5.dp))
          Text(
              stringResource(R.string.module_selection_description),
              fontSize = MaterialTheme.typography.bodyMedium.fontSize,
              lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface.copy(0.7F),
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth(.8F))
          Spacer(Modifier.height(20.dp))
          ModuleImportButton { importPopupState.value = !importPopupState.value }
          LazyColumn(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceEvenly,
          ) {
              // TODO: pass name, colour and id into modulechoice rather than everything
              items(items = ModuleLayer.availableModules) { module ->
                  ModuleChoice(
                      module.name,
                      module.author,
                      module.version,
                      module.js,
                      module.image,
                      module.usesExternalApi,
                      module.website,
                      if (module.backgroundColor.isNullOrBlank()) null
                      else Color(module.backgroundColor.toLong(16)),
                      if (module.foregroundColor.isNullOrBlank()) null
                      else Color(module.foregroundColor.toLong(16)),
                      onClick = {
                          // TODO: Add overload to update by id
                          ModuleLayer.updateSelectedModule(
                              ModuleModel(
                                  module.name,
                                  module.author,
                                  module.version,
                                  module.js,
                                  module.image,
                                  module.usesExternalApi,
                                  module.website,
                                  module.backgroundColor,
                                  module.foregroundColor
                              )
                          )
                          coroutineScope.launch { sheetState.hide() }
                  },
              )
            }
          }
        }
      },
      sheetBackgroundColor = MaterialTheme.colorScheme.surface,
      sheetShape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
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
                      ModuleLayer.selectedModule!!.name
                  )
              }

              androidx.compose.animation.AnimatedVisibility(importPopupState.value) {
                  AlertDialog(
                      onDismissRequest = { importPopupState.value = false },
                  ) {
                      Surface(
                          modifier = Modifier
                              .wrapContentSize()
                              .padding(28.dp),
                          shape = RoundedCornerShape(28.dp)
                      ) {
                      Column(
                          verticalArrangement = Arrangement.SpaceAround,
                          horizontalAlignment = Alignment.Start,
                      ) {
                        Text(
                            stringResource(R.string.import_module_header),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp))
                        Spacer(Modifier.height(0.dp))
                        OutlinedTextField(
                            value = importUrl,
                            onValueChange = { importUrl = it },
                            label = { Text(stringResource(R.string.import_module_desc)) },
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    coroutineScope.launch {
                                        ModuleLayer.enqueueRemoteInstall(
                                            importUrl.text
                                        )
                                        importUrl = TextFieldValue("")
                                    }
                                      importPopupState.value = false
                                    }))
                          Spacer(Modifier.height(24.dp))
                          Row(
                              horizontalArrangement = Arrangement.End,
                              modifier = Modifier
                                  .fillMaxWidth()
                                  .padding(0.dp, 8.dp, 0.dp, 16.dp)
                          ) {
                              TextButton(
                                  colors =
                                  ButtonDefaults.buttonColors(
                                      containerColor = Color.Transparent,
                                      contentColor = MaterialTheme.colorScheme.onSurface,
                                  ),
                                  onClick = {
                                      importPopupState.value = false
                                      importUrl = TextFieldValue("")
                                  }) {
                                    Text(stringResource(R.string.cancel))
                                  }

                              ElevatedButton(
                                  modifier = Modifier.padding(8.dp, 0.dp),
                                  shape = RoundedCornerShape(5.dp),
                                  colors =
                                      ButtonDefaults.buttonColors(
                                          containerColor = MaterialTheme.colorScheme.primary,
                                          contentColor = MaterialTheme.colorScheme.onPrimary,
                                      ),
                                  onClick = {
                                    coroutineScope.launch {
                                        ModuleLayer.enqueueRemoteInstall(
                                            importUrl.text
                                        )
                                        importUrl = TextFieldValue("")
                                    }
                                    importPopupState.value = false
                                  }) {
                                  Text(stringResource(R.string.import_module_button_confirm))
                              }
                          }
                      }
                      }
                  }
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
    name: String,
    author: String,
    version: String,
    js: String,
    image: String?,
    usesExternalApi: Boolean?,
    website: String,
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
      colors =
      if (backgroundColor != null) {
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
            modifier = Modifier.fillMaxWidth()) {
              if (image == null)
                  Icon(Icons.Default.Help, "Question Mark", modifier = Modifier.size(iconSize.dp))
              else
                  GlideImage(
                      imageModel = { image },
                      imageOptions =
                      ImageOptions(
                          contentScale = ContentScale.Fit,
                          alignment = Alignment.Center,
                          contentDescription = "Favicon for the $name module",
                          requestSize = IntSize(iconSizePx, iconSizePx)
                      ),
                      loading = {
                          Box(Modifier.matchParentSize()) {
                              CircularProgressIndicator(Modifier.align(Alignment.Center))
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
                    overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.Start) {
                  Text(
                      author,
                      color = foregroundColor?.copy(0.8F) ?: MaterialTheme.colorScheme.onSecondary,
                      fontSize = MaterialTheme.typography.bodySmall.fontSize,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                      "v$version",
                      color = foregroundColor?.copy(0.8F) ?: MaterialTheme.colorScheme.onSecondary,
                      fontSize = MaterialTheme.typography.bodySmall.fontSize,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis)
                }
              }
            }
      },
  )
}

@Composable
@Preview(name = "Module Choice Selector", showBackground = false)
fun ModuleChoice(@PreviewParameter(ModuleChoiceProvider::class) params: ModuleChoiceParams) {
  return ModuleChoice(
      params.name,
      params.author,
      params.version,
      params.js,
      params.image,
      params.usesExternalApi,
      params.website,
      params.backgroundColor,
      params.foregroundColor,
      {})
}

data class ModuleChoiceParams(
    val name: String,
    val author: String,
    val version: String,
    val js: String,
    val image: String?,
    val usesExternalApi: Boolean?,
    val website: String,
    val backgroundColor: Color?,
    val foregroundColor: Color?,
)

class ModuleChoiceProvider() : PreviewParameterProvider<ModuleChoiceParams> {
  override val values =
      sequenceOf(
          ModuleChoiceParams(
              "Zoro",
              "Inumaki",
              "1.0.0",
              "",
              "https://zoro.to/images/favicon.png?v=01",
              false,
              "",
              Color(0xFFffcb3d),
              null,
          ))
  override val count: Int
    get() = 1
}

@Composable
fun ModuleImportButton(onClick: () -> Unit) {
  val iconSize = 25

  // TODO: Add dotted outline
  Button(
      modifier = Modifier
          .fillMaxWidth(1F)
          .height(65.dp)
          .padding(vertical = 4.dp),
      colors =
      ButtonDefaults.buttonColors(
          containerColor = Color.Transparent,
          //                contentColor = foregroundColor
      ),
      shape = RoundedCornerShape(10.dp),
      onClick = onClick,
      content = {
          Row(
              horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
              Icon(
                  Icons.Default.Download,
                  "Import Module",
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
                Row(horizontalArrangement = Arrangement.Start) {
                  Text(
                      stringResource(R.string.import_module_desc),
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      fontSize = MaterialTheme.typography.bodySmall.fontSize,
                      fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.width(4.dp))
                }
              }
            }
      },
  )
}

@Composable
fun ContentSearchBar(modifier: Modifier, activeModuleName: String) {
  Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Search for content",
        modifier = Modifier.padding(start = 16.dp),
        tint = MaterialTheme.colorScheme.outline)
    Text(
        text = "Search using $activeModuleName",
        modifier = Modifier
            .weight(1f)
            .padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline
    )
    IconButton(modifier = Modifier.padding(end = 16.dp), onClick = {}) {
      Icon(Icons.Default.AccountCircle, "Your Profile", tint = MaterialTheme.colorScheme.outline)
    }
  }
}
