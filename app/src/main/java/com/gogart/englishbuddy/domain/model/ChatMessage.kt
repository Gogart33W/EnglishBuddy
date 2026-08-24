package com.gogart.englishbuddy.domain.model

data class ChatMessage(
    val content: String,
    val role: MessageRole
)

enum class MessageRole {
    USER, MODEL
}
