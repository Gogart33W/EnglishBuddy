package com.gogart.englishbuddy.ui

import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.ChatSession
import com.gogart.englishbuddy.data.remote.dto.DictionaryResponse
import com.gogart.englishbuddy.data.local.entity.DictionaryEntity
import com.gogart.englishbuddy.data.local.entity.MistakeEntity
import com.gogart.englishbuddy.data.local.entity.UserProfileEntity

data class ChatUiState(
    val sessions: List<ChatSession> = emptyList(),
    val currentSessionId: Long? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val wordDefinition: DictionaryResponse? = null,
    val isDictionaryLoading: Boolean = false,
    val savedWords: List<DictionaryEntity> = emptyList(),
    val allMistakes: List<MistakeEntity> = emptyList(),
    val userProfile: UserProfileEntity? = null,
    val dailyActivity: List<com.gogart.englishbuddy.data.local.entity.DailyActivityEntity> = emptyList(),
    val error: String? = null
)
