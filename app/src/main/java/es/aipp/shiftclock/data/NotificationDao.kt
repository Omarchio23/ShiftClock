package es.aipp.shiftclock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestampMillis DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>
}
