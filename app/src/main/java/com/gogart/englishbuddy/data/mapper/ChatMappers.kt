package com.gogart.englishbuddy.data.mapper

import com.gogart.englishbuddy.data.local.entity.ChatMessageEntity
import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.MessageRole

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        content = content,
        role = MessageRole.valueOf(role)
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        content = content,
        role = role.name
    )
}
