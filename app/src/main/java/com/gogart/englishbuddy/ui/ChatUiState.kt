package com.gogart.englishbuddy.ui

import com.gogart.englishbuddy.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
