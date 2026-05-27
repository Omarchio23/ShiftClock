package es.aipp.shiftclock.data

import kotlinx.coroutines.flow.Flow

class DataRepository(private val alarmDao: AlarmDao) {

    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarmsFlow()

    suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.deleteAlarm(alarm)
    }
    
    suspend fun getAlarmById(id: Int): AlarmEntity? {
        return alarmDao.getAlarmById(id)
    }
}
