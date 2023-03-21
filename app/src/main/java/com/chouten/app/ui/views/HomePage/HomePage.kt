package com.chouten.app.ui.views.HomePage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.chouten.app.R
import kotlinx.coroutines.launch
import java.time.format.TextStyle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomePage() {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Expanded,
    )

    val coroutineScope = rememberCoroutineScope()

    var selectedModule = "No Module"
    ModalBottomSheetLayout(
        sheetState = sheetState, sheetContent = {
            LazyColumn(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(15.dp)) {
                items(count = 1) {
                    Text(
                        stringResource(R.string.module_selection_header),
                        style = MaterialTheme.typography.h6)
                    Text(
                        stringResource(R.string.module_selection_description),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.fillMaxWidth(.8F))
                    Spacer(Modifier.height(20.dp))
                }
                items(count = 3) { ModuleChoice("Zoro", "Inumaki", "1.0.0") }
            }
        },
        sheetContentColor = MaterialTheme.colors.surface,
        sheetShape = RoundedCornerShape(10.dp)
    ) {

        ElevatedButton(onClick = {
            coroutineScope.launch {
                if (sheetState.isVisible) {
                    sheetState.hide()
                } else sheetState.show()
            }
        }) {
            Text("${selectedModule.uppercase()}", fontWeight = FontWeight.Bold)
        }

    }
}

@Composable
fun ModuleChoice(name: String, author: String, version: String) {

    androidx.compose.material3.Button(
        modifier = Modifier
            .fillMaxWidth(0.8F)
            .height(65.dp)
            .padding(5.dp),
        shape = RoundedCornerShape(10.dp),
        onClick = { },
        content = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.Help,
                    "Question Mark",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(name, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.Start) {
                        Text(
                            author,
                            color = Color(R.color.dynamic_accent_1_800),
                            fontSize = MaterialTheme.typography.caption.fontSize,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "v$version",
                            color = Color(R.color.dynamic_accent_1_800),
                            fontSize = MaterialTheme.typography.caption.fontSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    )
}