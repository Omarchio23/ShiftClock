package es.aipp.shiftclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val alarmId: Int,
    val alarmTitle: String,
    val eventType: String, // RANG, SNOOZED, STOPPED
    val timestampMillis: Long,
    val vibrated: Boolean,
    val durationSeconds: Int // Only applies to SNOOZED and STOPPED
)
