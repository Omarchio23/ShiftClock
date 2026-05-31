package es.aipp.shiftclock

import android.app.KeyguardManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import es.aipp.shiftclock.ui.components.SwipeToStopButton
import es.aipp.shiftclock.data.AppDatabase
import es.aipp.shiftclock.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var volumeJob: kotlinx.coroutines.Job? = null
    private var alarmStartTime: Long = 0L
    private var hasVibrated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        alarmStartTime = System.currentTimeMillis()
        super.onCreate(savedInstanceState)

        // Wake up screen and show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val alarmId = intent.getIntExtra("EXTRA_ALARM_ID", -1)
        val alarmTitle = intent.getStringExtra("EXTRA_ALARM_TITLE") ?: "Alarma"
        val isSnoozeEnabled = intent.getBooleanExtra("EXTRA_SNOOZE_ENABLED", true)
        val snoozeDurationMinutes = intent.getIntExtra("EXTRA_SNOOZE_DURATION", 9)
        
        val settingsRepository = es.aipp.shiftclock.data.SettingsRepository(this)
        val swipeDirection = settingsRepository.swipeDirection

        playAlarmSound()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.wake_up),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        if (isSnoozeEnabled && alarmId != -1) {
                            Button(
                                onClick = {
                                    val snoozeMillis = System.currentTimeMillis() + (snoozeDurationMinutes * 60_000L)
                                    val scheduler = AlarmScheduler(this@AlarmActivity)
                                    scheduler.scheduleAlarm(snoozeMillis, alarmId)
                                    saveNotificationEvent(alarmId, alarmTitle, "SNOOZED")
                                    stopAlarm()
                                    finish()
                                },
                                modifier = Modifier.size(width = 250.dp, height = 60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(stringResource(R.string.snooze, snoozeDurationMinutes), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiary)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        SwipeToStopButton(
                            text = stringResource(R.string.stop_alarm),
                            swipeDirection = swipeDirection,
                            onDismiss = {
                                saveNotificationEvent(alarmId, alarmTitle, "STOPPED")
                                stopAlarm()
                                
                                val isBedExitAlarm = intent.getBooleanExtra("EXTRA_IS_BED_EXIT", false)
                                val repo = es.aipp.shiftclock.data.SettingsRepository(this@AlarmActivity)
                                if (!isBedExitAlarm && repo.bedExitEnabled) {
                                    val serviceIntent = android.content.Intent(this@AlarmActivity, es.aipp.shiftclock.logic.BedExitService::class.java)
                                    serviceIntent.putExtra("EXTRA_ALARM_ID", alarmId)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        startForegroundService(serviceIntent)
                                    } else {
                                        startService(serviceIntent)
                                    }
                                }
                                
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun saveNotificationEvent(alarmId: Int, alarmTitle: String, eventType: String) {
        val durationSeconds = ((System.currentTimeMillis() - alarmStartTime) / 1000).toInt()
        val notification = NotificationEntity(
            alarmId = alarmId,
            alarmTitle = alarmTitle,
            eventType = eventType,
            timestampMillis = System.currentTimeMillis(),
            vibrated = hasVibrated,
            durationSeconds = durationSeconds
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(this@AlarmActivity).notificationDao().insertNotification(notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playAlarmSound() {
        val alarmVibrate = intent.getBooleanExtra("EXTRA_VIBRATE_ENABLED", true)
        val repository = es.aipp.shiftclock.data.SettingsRepository(this)
        val globalVibrate = repository.isGlobalVibrateEnabled
        val gradualVolume = repository.isGradualVolumeEnabled
        
        // "si queremos vibracion y que esté activada por defecto para todas las alarmas cuando suenen o si se apaga dependerá de la configuracion de cada alarma"
        // Interpretación: El switch global activa/desactiva la vibración para todas. El de la alarma decide por alarma.
        val shouldVibrate = globalVibrate && alarmVibrate
        hasVibrated = shouldVibrate

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
            if (gradualVolume) {
                ringtone?.volume = 0.1f
                volumeJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    for (i in 2..10) {
                        kotlinx.coroutines.delay(3000)
                        ringtone?.volume = i / 10f
                    }
                }
            } else {
                ringtone?.volume = 1.0f
            }
        }
        ringtone?.play()

        if (shouldVibrate) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build()
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0)
                vibrator?.vibrate(effect, audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build()
                vibrator?.vibrate(longArrayOf(0, 500, 500), 0, audioAttributes)
            }
        }
    }

    private fun stopAlarm() {
        volumeJob?.cancel()
        ringtone?.stop()
        vibrator?.cancel()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(1)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm() // Asegurar que el sonido pare si se destruye la actividad
    }
}
