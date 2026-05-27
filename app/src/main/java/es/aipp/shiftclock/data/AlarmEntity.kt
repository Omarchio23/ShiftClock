package es.aipp.shiftclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: PatternType,
    val hour: Int,
    val minute: Int,
    val patternData: String, // JSON serializado de la configuración específica
    val isActive: Boolean = true,
    val nextTriggerTimeMillis: Long = 0L,
    val isSnoozeEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 10,
    val isVibrateEnabled: Boolean = true,
    val creationDateMillis: Long = System.currentTimeMillis()
)

enum class PatternType {
    WEEKLY,
    SHIFT_CYCLIC,
    BIWEEKLY_CUSTODY,
    INTERVAL,
    REST_COUNTDOWN
}
