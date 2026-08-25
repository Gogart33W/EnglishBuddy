package com.gogart.englishbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary")
data class DictionaryEntity(
    @PrimaryKey
    val word: String,
    val transcription: String,
    val translationUk: String,
    val example: String,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
