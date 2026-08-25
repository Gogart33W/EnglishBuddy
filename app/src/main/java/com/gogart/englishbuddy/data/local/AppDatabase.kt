package com.gogart.englishbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gogart.englishbuddy.data.local.dao.*
import com.gogart.englishbuddy.data.local.entity.*

@Database(
    entities = [
        ChatMessageEntity::class,
        ChatSessionEntity::class,
        MistakeEntity::class,
        DictionaryEntity::class,
        UserProfileEntity::class,
        DailyActivityEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val chatMessageDao: ChatMessageDao
    abstract val chatSessionDao: ChatSessionDao
    abstract val mistakeDao: MistakeDao
    abstract val dictionaryDao: DictionaryDao
    abstract val userProfileDao: UserProfileDao
    abstract val dailyActivityDao: DailyActivityDao
}
