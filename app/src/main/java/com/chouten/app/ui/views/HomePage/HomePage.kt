package com.chouten.app.ui.views.HomePage

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.chouten.app.R
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun HomePage() {
  val sheetState =
      androidx.compose.material.rememberModalBottomSheetState(
          initialValue = ModalBottomSheetValue.Expanded,
      )

  val coroutineScope = rememberCoroutineScope()

  var selectedModule = "No Module"
  ModalBottomSheetLayout(
      sheetState = sheetState,
      sheetContent = {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(15.dp, 0.dp, 15.dp, 10.dp)) {
              items(count = 1) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()) {
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
              }
              items(count = 2) {
                ModuleChoice(
                    "Zoro",
                    "Inumaki",
                    "1.0.0",
                    "https://res.cloudinary.com/crunchbase-production/image/upload/c_lpad,f_auto,q_auto:eco,dpr_1/qe7kzhh0bo1qt9ohrxwb",
                    Color(0xFFffcb3d),
                )
                ModuleChoice(
                    "GogoAnime",
                    "Inumaki",
                    "1.0.0",
                    "https://res.cloudinary.com/crunchbase-production/image/upload/c_lpad,f_auto,q_auto:eco,dpr_1/qe7kzhh0bo1qt9ohrxwb",
                )
              }
            }
      },
      sheetBackgroundColor = MaterialTheme.colorScheme.surface,
      sheetShape = RoundedCornerShape(30.dp)) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
              ElevatedButton(
                  onClick = {
                    coroutineScope.launch {
                      if (sheetState.isVisible) {
                        sheetState.hide()
                      } else sheetState.show()
                    }
                  }) {
                    Text(selectedModule.uppercase(), fontWeight = FontWeight.Bold)
                  }
            }
      }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ModuleChoice(
    name: String,
    author: String,
    version: String? = "1.0.0",
    image: String? = null,
    color: Color? = null,
    fgColor: Color? = null
) {
  val iconSize = 40.dp
  Button(
      modifier = Modifier.fillMaxWidth(1F).height(65.dp).padding(vertical = 4.dp),
      colors =
          if (color != null) {
            ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = fgColor ?: MaterialTheme.colorScheme.onPrimaryContainer)
          } else ButtonDefaults.buttonColors(),
      shape = RoundedCornerShape(10.dp),
      onClick = {},
      content = {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
              if (image == null)
                  Icon(Icons.Default.Help, "Question Mark", modifier = Modifier.size(iconSize))
              else
                  GlideImage(
                      model = image,
                      contentDescription = "Favicon for the $name module",
                      modifier = Modifier.size(iconSize).clip(CircleShape),
                      contentScale = ContentScale.Fit,
                  )

              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    color = fgColor ?: MaterialTheme.colorScheme.onSecondary)
                Row(horizontalArrangement = Arrangement.Start) {
                  Text(
                      author,
                      color = fgColor?.copy(0.8F) ?: MaterialTheme.colorScheme.onSecondary,
                      fontSize = MaterialTheme.typography.bodySmall.fontSize,
                      fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                      "v$version",
                      color = fgColor?.copy(0.8F) ?: MaterialTheme.colorScheme.onSecondary,
                      fontSize = MaterialTheme.typography.bodySmall.fontSize,
                      fontWeight = FontWeight.SemiBold)
                }
              }
            }
      },
      //      contentPadding = PaddingValues(0.dp)
  )
}

@Composable
@Preview(name = "Module Choice Selector", showBackground = false)
fun ModuleChoice(@PreviewParameter(ModuleChoiceProvider::class) params: ModuleChoiceParams) {
  return ModuleChoice(
      params.name, params.author, params.version, params.imageUrl, params.color, params.fgColor)
}

data class ModuleChoiceParams(
    val name: String,
    val author: String,
    val version: String,
    val imageUrl: String?,
    val color: Color?,
    val fgColor: Color?
)

class ModuleChoiceProvider() : PreviewParameterProvider<ModuleChoiceParams> {
  override val values =
      sequenceOf(
          ModuleChoiceParams(
              "Zoro",
              "Inumaki",
              "1.0.0",
              "https://zoro.to/images/favicon.png?v=01",
              Color(0xFFffcb3d),
              null))
  override val count: Int
    get() = 1
}
