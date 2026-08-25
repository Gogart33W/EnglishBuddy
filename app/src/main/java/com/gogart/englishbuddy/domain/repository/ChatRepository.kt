package com.gogart.englishbuddy.domain.repository

import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.ChatSession
import com.gogart.englishbuddy.data.remote.dto.DictionaryResponse
import com.gogart.englishbuddy.data.local.entity.DictionaryEntity
import com.gogart.englishbuddy.data.local.entity.MistakeEntity
import com.gogart.englishbuddy.data.local.entity.UserProfileEntity
import com.gogart.englishbuddy.data.local.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getSessions(): Flow<List<ChatSession>>
    suspend fun createSession(title: String): Long
    suspend fun deleteSession(sessionId: Long)
    suspend fun updateSessionTitle(id: Long, title: String)
    
    fun getChatHistory(sessionId: Long): Flow<List<ChatMessage>>
    suspend fun sendMessage(sessionId: Long, content: String): Result<Unit>
    suspend fun clearHistory(sessionId: Long)

    suspend fun getWordDefinition(word: String): Result<DictionaryResponse>
    fun getSavedWords(): Flow<List<DictionaryEntity>>
    suspend fun toggleSaveWord(word: String, isSaved: Boolean)
    
    fun getMistakes(sessionId: Long? = null): Flow<List<MistakeEntity>>
    suspend fun deleteMistake(mistake: MistakeEntity)
    suspend fun resolveMistake(mistake: MistakeEntity)
    
    fun getUserProfile(): Flow<UserProfileEntity?>
    suspend fun updateUserProfile(profile: UserProfileEntity)

    fun getActivity(): Flow<List<DailyActivityEntity>>
    suspend fun trackActivity(type: ActivityType)
}

enum class ActivityType {
    MINUTE, MESSAGE, MISTAKE_RESOLVED
}
