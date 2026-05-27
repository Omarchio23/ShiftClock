package es.aipp.shiftclock.ui.main

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import es.aipp.shiftclock.R
import kotlinx.coroutines.delay
import kotlin.math.max
import android.media.Ringtone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(false) }
    var isAlarmRinging by remember { mutableStateOf(false) }
    var timeRemainingMillis by remember { mutableStateOf(0L) }
    
    var showMenu by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    
    var inputMinutes by remember { mutableStateOf(5) }
    var inputSeconds by remember { mutableStateOf(0) }

    var activeRingtone by remember { mutableStateOf<Ringtone?>(null) }
    var activeVibrator by remember { mutableStateOf<Vibrator?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            activeRingtone?.stop()
            activeVibrator?.cancel()
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val endTime = System.currentTimeMillis() + timeRemainingMillis
            while (isRunning && timeRemainingMillis > 0) {
                timeRemainingMillis = max(0L, endTime - System.currentTimeMillis())
                delay(50)
                
                if (timeRemainingMillis == 0L) {
                    isRunning = false
                    isAlarmRinging = true
                    
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone?.isLooping = true
                    }
                    ringtone?.play()
                    activeRingtone = ringtone
                    
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
                    }
                    activeVibrator = vibrator
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.tab_timer),
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
                modifier = Modifier.padding(32.dp).fillMaxWidth().aspectRatio(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (!isRunning && timeRemainingMillis == 0L && !isAlarmRinging) {
                        // Setup Mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            NumberPicker(
                                value = inputMinutes,
                                onValueChange = { inputMinutes = it },
                                range = 0..99
                            )
                            Text(" : ", style = MaterialTheme.typography.displayLarge)
                            NumberPicker(
                                value = inputSeconds,
                                onValueChange = { inputSeconds = it },
                                range = 0..59
                            )
                        }
                    } else {
                        // Countdown Mode
                        val minutes = (timeRemainingMillis / 60000)
                        val seconds = (timeRemainingMillis % 60000) / 1000
                        val milliseconds = (timeRemainingMillis % 1000) / 10

                        Text(
                            text = String.format("%02d:%02d.%02d", minutes, seconds, milliseconds),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isAlarmRinging) {
                    Button(
                        onClick = {
                            isAlarmRinging = false
                            activeRingtone?.stop()
                            activeVibrator?.cancel()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.width(200.dp).height(64.dp)
                    ) {
                        Text(stringResource(R.string.stop_alarm), style = MaterialTheme.typography.titleLarge)
                    }
                } else {
                    Button(
                        onClick = {
                            if (!isRunning && timeRemainingMillis == 0L) {
                                timeRemainingMillis = (inputMinutes * 60000L) + (inputSeconds * 1000L)
                                if (timeRemainingMillis > 0) {
                                    isRunning = true
                                }
                            } else {
                                isRunning = !isRunning
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(
                            if (isRunning) stringResource(R.string.pause)
                            else if (timeRemainingMillis > 0) stringResource(R.string.resume)
                            else stringResource(R.string.start)
                        )
                    }

                    Button(
                        onClick = {
                            isRunning = false
                            timeRemainingMillis = 0L
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
}

@Composable
fun NumberPicker(value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }) {
            Text("▲", style = MaterialTheme.typography.titleLarge)
        }
        Text(String.format("%02d", value), style = MaterialTheme.typography.displayMedium)
        IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }) {
            Text("▼", style = MaterialTheme.typography.titleLarge)
        }
    }
}


