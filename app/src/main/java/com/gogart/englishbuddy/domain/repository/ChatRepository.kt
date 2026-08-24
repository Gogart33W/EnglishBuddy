package com.gogart.englishbuddy.domain.repository

import com.gogart.englishbuddy.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatHistory(): Flow<List<ChatMessage>>
    suspend fun sendMessage(content: String): Result<Unit>
}
