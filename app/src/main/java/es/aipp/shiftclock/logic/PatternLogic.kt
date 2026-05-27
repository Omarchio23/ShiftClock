package es.aipp.shiftclock.logic

import es.aipp.shiftclock.data.AlarmEntity
import es.aipp.shiftclock.data.PatternType
import com.google.gson.Gson
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

interface PatternStrategy {
    fun calculateNextTrigger(alarm: AlarmEntity, after: LocalDateTime = LocalDateTime.now()): Long
}

object PatternStrategyFactory {
    fun getStrategy(type: PatternType): PatternStrategy {
        return when (type) {
            PatternType.WEEKLY -> WeeklyPatternStrategy()
            PatternType.SHIFT_CYCLIC -> ShiftPatternStrategy()
            PatternType.BIWEEKLY_CUSTODY -> BiweeklyPatternStrategy()
            PatternType.INTERVAL -> IntervalPatternStrategy()
            PatternType.REST_COUNTDOWN -> RestPatternStrategy()
        }
    }
}

// Modelos de datos para JSON
data class ShiftPatternData(val daysOn: Int, val daysOff: Int, val startDateMillis: Long)
data class BiweeklyPatternData(val activeDaysOfWeek: List<Int>, val weekParity: Int) // 0=Ambas, 1=Impar, 2=Par
data class IntervalPatternData(val intervalDays: Int, val startDateMillis: Long)
data class RestPatternData(val hoursOfRest: Int)
data class WeeklyPatternData(val activeDays: List<Int>) // 1=Monday, 7=Sunday

class WeeklyPatternStrategy : PatternStrategy {
    override fun calculateNextTrigger(alarm: AlarmEntity, after: LocalDateTime): Long {
        val data = Gson().fromJson(alarm.patternData, WeeklyPatternData::class.java)
        if (data.activeDays.isEmpty()) return -1L

        val alarmTime = LocalTime.of(alarm.hour, alarm.minute)
        
        var checkDateTime = after
        if (after.toLocalTime().isAfter(alarmTime) || after.toLocalTime() == alarmTime) {
            checkDateTime = checkDateTime.plusDays(1)
        }
        checkDateTime = checkDateTime.with(alarmTime)

        for (i in 0..7) { // Check up to 7 days ahead
            val dayOfWeek = checkDateTime.dayOfWeek.value
            if (data.activeDays.contains(dayOfWeek)) {
                return checkDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            checkDateTime = checkDateTime.plusDays(1)
        }
        return -1L
    }
}

class ShiftPatternStrategy : PatternStrategy {
    override fun calculateNextTrigger(alarm: AlarmEntity, after: LocalDateTime): Long {
        val data = Gson().fromJson(alarm.patternData, ShiftPatternData::class.java)
        val cycleLength = data.daysOn + data.daysOff
        if (cycleLength == 0) return -1
        
        val cycleStartDate = Instant.ofEpochMilli(data.startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val alarmTime = LocalTime.of(alarm.hour, alarm.minute)
        
        var checkDate = after.toLocalDate()
        if (after.toLocalTime().isAfter(alarmTime) || after.toLocalTime() == alarmTime) {
            checkDate = checkDate.plusDays(1)
        }

        while (true) {
            val daysSinceStart = ChronoUnit.DAYS.between(cycleStartDate, checkDate)
            if (daysSinceStart < 0) {
                checkDate = cycleStartDate
                continue
            }
            val dayInCycle = (daysSinceStart % cycleLength).toInt()
            if (dayInCycle < data.daysOn) {
                return LocalDateTime.of(checkDate, alarmTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                checkDate = checkDate.plusDays((cycleLength - dayInCycle).toLong())
            }
        }
    }
}

class BiweeklyPatternStrategy : PatternStrategy {
    override fun calculateNextTrigger(alarm: AlarmEntity, after: LocalDateTime): Long {
        // Implementación simplificada
        return -1L
    }
}

class IntervalPatternStrategy : PatternStrategy {
    override fun calculateNextTrigger(alarm: AlarmEntity, after: LocalDateTime): Long {
        // Implementación simplificada
        return -1L
    }
}

class RestPatternStrategy : PatternStrategy {
    override fun calculateNextTrigger(alarm: AlarmEntity, after: LocalDateTime): Long {
        val data = Gson().fromJson(alarm.patternData, RestPatternData::class.java)
        return after.plusHours(data.hoursOfRest.toLong()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
