package com.gogart.englishbuddy.data.local.dao

import androidx.room.*
import com.gogart.englishbuddy.data.local.entity.MistakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistakes WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getMistakesBySession(sessionId: Long): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes ORDER BY timestamp DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity)

    @Query("SELECT * FROM mistakes WHERE sessionId = :sessionId AND originalText = :original AND correctedText = :corrected LIMIT 1")
    suspend fun findMistake(sessionId: Long, original: String, corrected: String): MistakeEntity?

    @Query("UPDATE mistakes SET repeatCount = repeatCount + 1, timestamp = :timestamp WHERE id = :id")
    suspend fun incrementRepeatCount(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)
}
