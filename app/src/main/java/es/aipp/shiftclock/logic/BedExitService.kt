package es.aipp.shiftclock.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import es.aipp.shiftclock.AlarmActivity
import es.aipp.shiftclock.MainActivity
import es.aipp.shiftclock.R
import es.aipp.shiftclock.data.SettingsRepository
import kotlinx.coroutines.*
import kotlin.math.abs

class BedExitService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    private var initialX: Float? = null
    private var initialY: Float? = null
    private var initialZ: Float? = null

    private var threshold: Float = 2.0f
    private var windowMinutes: Int = 10
    private var motionTimeSeconds: Int = 5
    private var originalAlarmId: Int = -1
    
    private var firstMotionTime: Long? = null
    private var lastMotionTime: Long? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val repository = SettingsRepository(this)
        if (!repository.bedExitEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        threshold = repository.bedExitSensitivity
        windowMinutes = repository.bedExitWindowMinutes
        motionTimeSeconds = repository.bedExitMotionTimeSeconds
        originalAlarmId = intent?.getIntExtra("EXTRA_ALARM_ID", -1) ?: -1

        startForegroundServiceNotification()

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        serviceScope.launch {
            delay(windowMinutes * 60 * 1000L)
            triggerSafetyAlarm()
        }

        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "bed_exit_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BedExit Detect",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background monitoring for BedExit Detect"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.bed_exit_service_notification_title))
            .setContentText(getString(R.string.bed_exit_service_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this icon exists
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1001, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (initialX == null || initialY == null || initialZ == null) {
            initialX = x
            initialY = y
            initialZ = z
            return
        }

        val dx = abs(x - initialX!!)
        val dy = abs(y - initialY!!)
        val dz = abs(z - initialZ!!)
        
        val currentTime = System.currentTimeMillis()

        if (dx > threshold || dy > threshold || dz > threshold) {
            // Is moving
            if (firstMotionTime == null) {
                firstMotionTime = currentTime
            }
            lastMotionTime = currentTime
            
            // Check if it has been moving long enough
            if (currentTime - firstMotionTime!! >= motionTimeSeconds * 1000L) {
                stopMonitoringAndExit()
            }
        } else {
            // Not moving enough. If it's been quiet for more than 1.5 seconds, reset continuous motion.
            if (lastMotionTime != null && currentTime - lastMotionTime!! > 1500L) {
                firstMotionTime = null
            }
            // Slowly adjust the baseline to account for changing positions without shaking
            initialX = initialX!! + (x - initialX!!) * 0.1f
            initialY = initialY!! + (y - initialY!!) * 0.1f
            initialZ = initialZ!! + (z - initialZ!!) * 0.1f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    private fun triggerSafetyAlarm() {
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_ALARM_TITLE", getString(R.string.bed_exit_detect))
            putExtra("EXTRA_IS_BED_EXIT", true)
            putExtra("EXTRA_ALARM_ID", originalAlarmId)
        }
        startActivity(alarmIntent)
        stopMonitoringAndExit()
    }

    private fun stopMonitoringAndExit() {
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
