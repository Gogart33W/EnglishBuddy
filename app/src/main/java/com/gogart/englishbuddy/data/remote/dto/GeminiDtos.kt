package com.gogart.englishbuddy.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction? = null,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null
)

@Serializable
data class ResponseSchema(
    val type: String,
    val properties: Map<String, ResponseSchemaProperty>? = null,
    val required: List<String>? = null
)

@Serializable
data class ResponseSchemaProperty(
    val type: String,
    val description: String? = null
)

@Serializable
data class TutorResponse(
    val hasCorrection: Boolean,
    val errorOriginal: String? = null,
    val errorCorrected: String? = null,
    val errorExplanationUk: String? = null,
    val tutorResponse: String,
    val practicePrompt: String? = null
)

@Serializable
data class DictionaryResponse(
    val word: String,
    val transcription: String,
    val translation: String,
    val example: String
)

@Serializable
data class SystemInstruction(
    val parts: List<Part>
)

@Serializable
data class Content(
    val role: String,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val usageMetadata: UsageMetadata? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null
)

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)
