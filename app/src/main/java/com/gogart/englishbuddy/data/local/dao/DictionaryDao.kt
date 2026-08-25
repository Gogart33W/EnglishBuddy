package com.gogart.englishbuddy.data.local.dao

import androidx.room.*
import com.gogart.englishbuddy.data.local.entity.DictionaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionary WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): DictionaryEntity?

    @Query("SELECT * FROM dictionary WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedWords(): Flow<List<DictionaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: DictionaryEntity)

    @Query("UPDATE dictionary SET isSaved = :isSaved WHERE word = :word")
    suspend fun updateSavedStatus(word: String, isSaved: Boolean)

    @Delete
    suspend fun deleteWord(word: DictionaryEntity)
}
