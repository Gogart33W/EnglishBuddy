package com.gogart.englishbuddy.data.local.dao

import androidx.room.*
import com.gogart.englishbuddy.data.local.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    suspend fun getActivity(date: String): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    fun getAllActivity(): Flow<List<DailyActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: DailyActivityEntity)

    @Query("UPDATE daily_activity SET activeMinutes = activeMinutes + 1 WHERE date = :date")
    suspend fun incrementMinutes(date: String)

    @Query("UPDATE daily_activity SET messagesSent = messagesSent + 1 WHERE date = :date")
    suspend fun incrementMessages(date: String)

    @Query("UPDATE daily_activity SET mistakesResolved = mistakesResolved + 1 WHERE date = :date")
    suspend fun incrementMistakes(date: String)
}
