package com.gogart.englishbuddy.data.repository

import com.gogart.englishbuddy.BuildConfig
import com.gogart.englishbuddy.data.remote.GeminiApiService
import com.gogart.englishbuddy.data.remote.dto.*
import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.MessageRole
import com.gogart.englishbuddy.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException

class ChatRepositoryImpl(
    private val apiService: GeminiApiService
) : ChatRepository {

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    override fun getChatHistory(): Flow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val systemInstruction = SystemInstruction(
        parts = listOf(
            Part(
                text = """
                    You are Buddy, a friendly American friend and English tutor. 
                    Your personality is helpful, encouraging, and informal.
                    Rules for your responses:
                    1. If the user makes a mistake in English, provide a clear correction at the VERY TOP of your message.
                    2. Provide Ukrainian translations in square brackets [...] for difficult words or phrases.
                    3. Keep the conversation natural as if talking to a friend.
                """.trimIndent()
            )
        )
    )

    override suspend fun sendMessage(content: String): Result<Unit> {
        val userMessage = ChatMessage(content, MessageRole.USER)
        _chatHistory.update { it + userMessage }

        val slidingWindow = _chatHistory.value.takeLast(15)
        val request = GeminiRequest(
            contents = slidingWindow.map { message ->
                Content(
                    role = if (message.role == MessageRole.USER) "user" else "model",
                    parts = listOf(Part(text = message.content))
                )
            },
            systemInstruction = systemInstruction
        )

        return try {
            val response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
            if (response.isSuccessful) {
                val modelResponse = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (modelResponse != null) {
                    _chatHistory.update { it + ChatMessage(modelResponse, MessageRole.MODEL) }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty response from Gemini"))
                }
            } else {
                val errorMsg = when (response.code()) {
                    429 -> "Rate limit exceeded. Please wait a moment."
                    else -> "API Error: ${response.code()} ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network timeout or connection error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
