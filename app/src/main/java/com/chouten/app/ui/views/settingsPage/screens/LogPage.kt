package com.chouten.app.ui.views.settingsPage.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chouten.app.R
import com.chouten.app.data.LogDataLayer
import com.chouten.app.data.LogEntry
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogPage(
    provider: LogDataLayer, navController: NavController
) {

    val logs = remember {
        provider.logEntries.asReversed()
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Text(stringResource(R.string.settings_submenu_log))
            }
        })
    }) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // TODO: Not cut off the results horribly - floating bar?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = { provider.clearLogs() },
                    enabled = logs.isNotEmpty()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.log__action_clear),
                        )
                        Text(
                            text = stringResource(R.string.log__action_clear), fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (logs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp, 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.log__placeholder)
                    )
                }
            } else LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    logs
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.padding(16.dp, 0.dp)
                    ) {
                        LogEntryCard(it)
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntryCard(
    entry: LogEntry
) {
    var isMessageExpanded by remember {
        mutableStateOf(false)
    }

    Card(
        colors = if (entry.isError) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        } else CardDefaults.cardColors(), modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlideImage(
                        imageModel = { entry.module.meta.icon },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                            contentDescription = "Favicon for the ${entry.module.name} module",
                            requestSize = IntSize(128, 128)
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
                            .size(36.dp)
                            .clip(CircleShape),
                    )

                    Column {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = entry.module.name,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(modifier = Modifier
                .padding(8.dp)
                .clickable {
                    isMessageExpanded = !isMessageExpanded
                }
                .animateContentSize(),
                text = entry.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (isMessageExpanded) Int.MAX_VALUE else 4)
        }
    }
}