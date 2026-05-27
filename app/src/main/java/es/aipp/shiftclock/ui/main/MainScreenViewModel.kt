package es.aipp.shiftclock.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.aipp.shiftclock.AlarmScheduler
import es.aipp.shiftclock.data.AlarmEntity
import es.aipp.shiftclock.data.AppDatabase
import es.aipp.shiftclock.data.DataRepository
import es.aipp.shiftclock.logic.PatternStrategyFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DataRepository
    val allAlarms: Flow<List<AlarmEntity>>

    init {
        val alarmDao = AppDatabase.getDatabase(application).alarmDao()
        repository = DataRepository(alarmDao)
        allAlarms = repository.allAlarms
    }

    fun insertAndScheduleAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        val id = repository.insertAlarm(alarm).toInt()
        val savedAlarm = alarm.copy(id = id)
        
        val strategy = PatternStrategyFactory.getStrategy(savedAlarm.type)
        val nextTrigger = strategy.calculateNextTrigger(savedAlarm, LocalDateTime.now())
        
        if (nextTrigger != -1L) {
            val updatedAlarm = savedAlarm.copy(nextTriggerTimeMillis = nextTrigger)
            repository.updateAlarm(updatedAlarm)
            val scheduler = AlarmScheduler(getApplication())
            scheduler.scheduleAlarm(nextTrigger, id)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        repository.deleteAlarm(alarm)
        val scheduler = AlarmScheduler(getApplication())
        scheduler.cancelAlarm(alarm.id)
    }

    suspend fun getAlarmById(id: Int): AlarmEntity? {
        return repository.getAlarmById(id)
    }

    fun updateAndScheduleAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        repository.updateAlarm(alarm)
        val strategy = PatternStrategyFactory.getStrategy(alarm.type)
        val nextTrigger = strategy.calculateNextTrigger(alarm, LocalDateTime.now())
        
        if (nextTrigger != -1L) {
            val updatedAlarm = alarm.copy(nextTriggerTimeMillis = nextTrigger)
            repository.updateAlarm(updatedAlarm)
            val scheduler = AlarmScheduler(getApplication())
            scheduler.scheduleAlarm(nextTrigger, alarm.id)
        } else {
            val scheduler = AlarmScheduler(getApplication())
            scheduler.cancelAlarm(alarm.id)
        }
    }
}
