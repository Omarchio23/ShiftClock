package es.aipp.shiftclock.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import es.aipp.shiftclock.R
import es.aipp.shiftclock.data.AlarmEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainScreenViewModel = viewModel(),
    onCreateNewAlarm: () -> Unit,
    onEditAlarm: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val alarms by viewModel.allAlarms.collectAsState(initial = emptyList())
    
    var alarmToDelete by remember { mutableStateOf<AlarmEntity?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    
    var showAbout by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (alarmToDelete != null) {
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = { Text(stringResource(R.string.delete_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        alarmToDelete?.let { viewModel.deleteAlarm(it) }
                        alarmToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAbout) {
        es.aipp.shiftclock.ui.components.AboutDialog(onDismiss = { showAbout = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.alarms_title),
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNewAlarm,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_alarm_button))
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_alarms), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onClick = { onEditAlarm(alarm.id) },
                        onDelete = { alarmToDelete = alarm }
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: AlarmEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val nextTriggerTime = if (alarm.nextTriggerTimeMillis > 0) {
        Instant.ofEpochMilli(alarm.nextTriggerTimeMillis).atZone(ZoneId.systemDefault()).format(formatter)
    } else {
        "--"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp, 
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alarm.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val typeText = if (alarm.type == es.aipp.shiftclock.data.PatternType.WEEKLY) {
                    stringResource(R.string.type_weekly)
                } else {
                    stringResource(R.string.type_shift_cyclic)
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = typeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${String.format("%02d", alarm.hour)}:${String.format("%02d", alarm.minute)}",
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete, 
                        contentDescription = stringResource(R.string.delete_content_desc), 
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            
            val nextAlarmText = if (alarm.nextTriggerTimeMillis > 0) {
                stringResource(R.string.next_alarm_date, nextTriggerTime)
            } else {
                nextTriggerTime
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Info, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp), 
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = nextAlarmText, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
