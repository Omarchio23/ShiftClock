package es.aipp.shiftclock.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import es.aipp.shiftclock.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(onNavigateToSettings: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var timeMillis by remember { mutableStateOf(0L) }
    var showMenu by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - timeMillis
            while (true) {
                timeMillis = System.currentTimeMillis() - startTime
                delay(10) // Update every 10ms for smooth UI
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.tab_stopwatch),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_title)) },
                            onClick = { 
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.about)) },
                            onClick = { 
                                showMenu = false
                                showAbout = true 
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (showAbout) {
            es.aipp.shiftclock.ui.components.AboutDialog(onDismiss = { showAbout = false })
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(48.dp)
                ) {
                    val minutes = (timeMillis / 60000)
                    val seconds = (timeMillis % 60000) / 1000
                    val milliseconds = (timeMillis % 1000) / 10

                    Text(
                        text = String.format("%02d:%02d.%02d", minutes, seconds, milliseconds),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(
                        if (isRunning) stringResource(R.string.pause)
                        else if (timeMillis > 0) stringResource(R.string.resume)
                        else stringResource(R.string.start)
                    )
                }

                Button(
                    onClick = {
                        isRunning = false
                        timeMillis = 0L
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(stringResource(R.string.reset))
                }
            }
        }
    }
}
