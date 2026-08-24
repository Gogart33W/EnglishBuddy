package com.gogart.englishbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gogart.englishbuddy.data.local.dao.ChatMessageDao
import com.gogart.englishbuddy.data.local.entity.ChatMessageEntity

@Database(entities = [ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val chatMessageDao: ChatMessageDao
}
