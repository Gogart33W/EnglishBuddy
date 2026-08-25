package com.gogart.englishbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val cefrLevel: String = "A1",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = "", // format "yyyy-MM-dd"
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)
