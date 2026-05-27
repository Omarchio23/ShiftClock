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

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var volumeJob: kotlinx.coroutines.Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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
        val isSnoozeEnabled = intent.getBooleanExtra("EXTRA_SNOOZE_ENABLED", true)
        val snoozeDurationMinutes = intent.getIntExtra("EXTRA_SNOOZE_DURATION", 9)

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

                        Button(
                            onClick = {
                                stopAlarm()
                                finish()
                            },
                            modifier = Modifier.size(width = 250.dp, height = 80.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.stop_alarm), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
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
                // Patrón de vibración: 0ms retraso, vibra 500ms, pausa 500ms, repetir (0)
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
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
