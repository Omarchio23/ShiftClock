package es.aipp.shiftclock.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.aipp.shiftclock.R
import es.aipp.shiftclock.data.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    
    var isGlobalVibrateEnabled by remember { mutableStateOf(repository.isGlobalVibrateEnabled) }
    var isGradualVolumeEnabled by remember { mutableStateOf(repository.isGradualVolumeEnabled) }
    var swipeDirection by remember { mutableStateOf(repository.swipeDirection) }
    var expandedSwipeDropdown by remember { mutableStateOf(false) }

    var bedExitEnabled by remember { mutableStateOf(repository.bedExitEnabled) }
    var bedExitWindowMinutes by remember { mutableStateOf(repository.bedExitWindowMinutes.toFloat()) }
    var bedExitMotionTimeSeconds by remember { mutableStateOf(repository.bedExitMotionTimeSeconds.toFloat()) }
    var bedExitSensitivity by remember { mutableStateOf(repository.bedExitSensitivity) }

    var infoDialogTitle by remember { mutableStateOf("") }
    var infoDialogText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_alarms_category).uppercase(), 
                style = MaterialTheme.typography.labelLarge, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
            
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Global Vibrate Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(stringResource(R.string.settings_global_vibrate_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_global_vibrate_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isGlobalVibrateEnabled,
                            onCheckedChange = { 
                                isGlobalVibrateEnabled = it
                                repository.isGlobalVibrateEnabled = it
                            }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    // Gradual Volume Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(stringResource(R.string.settings_gradual_volume_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_gradual_volume_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isGradualVolumeEnabled,
                            onCheckedChange = { 
                                isGradualVolumeEnabled = it
                                repository.isGradualVolumeEnabled = it
                            }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    // Swipe Direction Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(stringResource(R.string.settings_swipe_direction_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_swipe_direction_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box {
                            TextButton(onClick = { expandedSwipeDropdown = true }) {
                                Text(when (swipeDirection) {
                                    "LEFT" -> stringResource(R.string.swipe_left)
                                    "BOTH" -> stringResource(R.string.swipe_both)
                                    else -> stringResource(R.string.swipe_right)
                                })
                            }
                            DropdownMenu(
                                expanded = expandedSwipeDropdown,
                                onDismissRequest = { expandedSwipeDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.swipe_right)) },
                                    onClick = {
                                        swipeDirection = "RIGHT"
                                        repository.swipeDirection = "RIGHT"
                                        expandedSwipeDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.swipe_left)) },
                                    onClick = {
                                        swipeDirection = "LEFT"
                                        repository.swipeDirection = "LEFT"
                                        expandedSwipeDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.swipe_both)) },
                                    onClick = {
                                        swipeDirection = "BOTH"
                                        repository.swipeDirection = "BOTH"
                                        expandedSwipeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.bed_exit_detect).uppercase(), 
                style = MaterialTheme.typography.labelLarge, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
            
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(stringResource(R.string.bed_exit_detect), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.bed_exit_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = bedExitEnabled,
                            onCheckedChange = { 
                                bedExitEnabled = it
                                repository.bedExitEnabled = it
                            }
                        )
                    }
                    
                    if (bedExitEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${stringResource(R.string.bed_exit_window_label)}: ${bedExitWindowMinutes.toInt()} min", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                val tTitle1 = stringResource(R.string.info_window_title)
                                val tDesc1 = stringResource(R.string.info_window_desc)
                                IconButton(onClick = { infoDialogTitle = tTitle1; infoDialogText = tDesc1 }) {
                                    Icon(Icons.Filled.Info, contentDescription = "Info", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Slider(
                                value = bedExitWindowMinutes,
                                onValueChange = { 
                                    bedExitWindowMinutes = it
                                    repository.bedExitWindowMinutes = it.toInt()
                                },
                                valueRange = 1f..10f,
                                steps = 8
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${stringResource(R.string.bed_exit_motion_time_label)}: ${bedExitMotionTimeSeconds.toInt()} s", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                val tTitle2 = stringResource(R.string.info_motion_time_title)
                                val tDesc2 = stringResource(R.string.info_motion_time_desc)
                                IconButton(onClick = { infoDialogTitle = tTitle2; infoDialogText = tDesc2 }) {
                                    Icon(Icons.Filled.Info, contentDescription = "Info", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Slider(
                                value = bedExitMotionTimeSeconds,
                                onValueChange = { 
                                    bedExitMotionTimeSeconds = it
                                    repository.bedExitMotionTimeSeconds = it.toInt()
                                },
                                valueRange = 2f..10f,
                                steps = 7
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${stringResource(R.string.bed_exit_sensitivity_label)}: ${String.format("%.1f", bedExitSensitivity)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                val tTitle3 = stringResource(R.string.info_sensitivity_title)
                                val tDesc3 = stringResource(R.string.info_sensitivity_desc)
                                IconButton(onClick = { infoDialogTitle = tTitle3; infoDialogText = tDesc3 }) {
                                    Icon(Icons.Filled.Info, contentDescription = "Info", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Slider(
                                value = bedExitSensitivity,
                                onValueChange = { 
                                    bedExitSensitivity = it
                                    repository.bedExitSensitivity = it
                                },
                                valueRange = 0.5f..10.0f,
                                steps = 19
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp)) // Extra padding at bottom
        }
    }
    
    if (infoDialogTitle.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { infoDialogTitle = "" },
            title = { Text(infoDialogTitle) },
            text = { Text(infoDialogText) },
            confirmButton = {
                TextButton(onClick = { infoDialogTitle = "" }) {
                    Text(stringResource(R.string.got_it))
                }
            }
        )
    }
}
