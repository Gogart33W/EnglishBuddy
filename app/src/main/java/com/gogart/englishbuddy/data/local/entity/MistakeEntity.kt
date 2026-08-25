package com.gogart.englishbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mistakes",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val originalText: String,
    val correctedText: String,
    val explanation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val repeatCount: Int = 1,
    val intervalDays: Int = 0,
    val nextReviewTimestamp: Long = 0,
    val isMastered: Boolean = false
)
