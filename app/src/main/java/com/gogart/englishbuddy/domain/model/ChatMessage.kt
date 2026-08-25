package com.gogart.englishbuddy.domain.model

data class ChatMessage(
    val id: Long = 0,
    val sessionId: Long,
    val content: String,
    val role: MessageRole
)

enum class MessageRole {
    USER, MODEL
}
