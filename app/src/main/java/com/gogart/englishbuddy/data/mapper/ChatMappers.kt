package com.gogart.englishbuddy.data.mapper

import com.gogart.englishbuddy.data.local.entity.ChatMessageEntity
import com.gogart.englishbuddy.data.local.entity.ChatSessionEntity
import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.ChatSession
import com.gogart.englishbuddy.domain.model.MessageRole

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        sessionId = sessionId,
        content = content,
        role = MessageRole.valueOf(role)
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        content = content,
        role = role.name
    )
}

fun ChatSessionEntity.toDomain(): ChatSession {
    return ChatSession(
        id = id,
        title = title,
        updatedAt = updatedAt
    )
}
