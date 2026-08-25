package com.gogart.englishbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val chatMessageDao: ChatMessageDao
    abstract val chatSessionDao: ChatSessionDao
    abstract val mistakeDao: MistakeDao
    abstract val dictionaryDao: DictionaryDao
    abstract val userProfileDao: UserProfileDao
    abstract val dailyActivityDao: DailyActivityDao

    companion object {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes, just moving away from destructive migration
            }
        }
    }
}
