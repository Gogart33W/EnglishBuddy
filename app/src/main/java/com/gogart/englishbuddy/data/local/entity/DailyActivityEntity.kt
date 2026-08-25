package com.gogart.englishbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey
    val date: String, // format "yyyy-MM-dd"
    val activeMinutes: Int = 0,
    val messagesSent: Int = 0,
    val mistakesResolved: Int = 0
)
