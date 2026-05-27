package es.aipp.shiftclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import es.aipp.shiftclock.data.AppDatabase
import es.aipp.shiftclock.logic.PatternStrategyFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("EXTRA_ALARM_ID", -1)
        if (alarmId == -1) return
        
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val alarmDao = db.alarmDao()
                val alarm = alarmDao.getAlarmById(alarmId)
                
                if (alarm != null && alarm.isActive) {
                    showNotification(context, alarmId, alarm)

                    val strategy = PatternStrategyFactory.getStrategy(alarm.type)
                    val nextTrigger = strategy.calculateNextTrigger(alarm, LocalDateTime.now())
                    
                    if (nextTrigger != -1L) {
                        val updatedAlarm = alarm.copy(nextTriggerTimeMillis = nextTrigger)
                        alarmDao.updateAlarm(updatedAlarm)
                        val scheduler = AlarmScheduler(context)
                        scheduler.scheduleAlarm(nextTrigger, alarmId)
                    }
                }
            } catch (e: Exception) {
                Log.e("ShiftClock", "Error executing alarm receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, alarmId: Int, alarm: es.aipp.shiftclock.data.AlarmEntity) {
        val channelId = "alarm_channel_silent_v1"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Alarmas Silenciosas",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setBypassDnd(true)
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_SNOOZE_ENABLED", alarm.isSnoozeEnabled)
            putExtra("EXTRA_SNOOZE_DURATION", alarm.snoozeDurationMinutes)
            putExtra("EXTRA_VIBRATE_ENABLED", alarm.isVibrateEnabled)
        }
        
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            Log.e("ShiftClock", "Failed to start AlarmActivity from background", e)
        }

        val fullScreenPendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setDefaults(0)
            .setSound(null)

        notificationManager.notify(1, notificationBuilder.build())
    }
}
