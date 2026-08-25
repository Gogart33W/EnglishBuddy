package com.gogart.englishbuddy.data.repository

import android.util.Log
import com.gogart.englishbuddy.BuildConfig
import com.gogart.englishbuddy.data.local.dao.ChatMessageDao
import com.gogart.englishbuddy.data.local.dao.ChatSessionDao
import com.gogart.englishbuddy.data.local.dao.*
import com.gogart.englishbuddy.data.local.entity.*
import com.gogart.englishbuddy.data.remote.GeminiApiService
import com.gogart.englishbuddy.data.remote.dto.*
import com.gogart.englishbuddy.data.mapper.toDomain
import com.gogart.englishbuddy.data.mapper.toEntity
import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.ChatSession
import com.gogart.englishbuddy.domain.model.MessageRole
import com.gogart.englishbuddy.domain.repository.ActivityType
import com.gogart.englishbuddy.domain.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatRepositoryImpl(
    private val apiService: GeminiApiService,
    private val chatDao: ChatMessageDao,
    private val sessionDao: ChatSessionDao,
    private val mistakeDao: MistakeDao,
    private val dictionaryDao: DictionaryDao,
    private val userProfileDao: UserProfileDao,
    private val activityDao: DailyActivityDao
) : ChatRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val tutorResponseSchema = ResponseSchema(
        type = "OBJECT",
        properties = mapOf(
            "hasCorrection" to ResponseSchemaProperty(type = "BOOLEAN"),
            "errorOriginal" to ResponseSchemaProperty(type = "STRING"),
            "errorCorrected" to ResponseSchemaProperty(type = "STRING"),
            "errorExplanationUk" to ResponseSchemaProperty(type = "STRING"),
            "tutorResponse" to ResponseSchemaProperty(type = "STRING"),
            "practicePrompt" to ResponseSchemaProperty(type = "STRING")
        ),
        required = listOf("hasCorrection", "tutorResponse")
    )

    private val dictionaryResponseSchema = ResponseSchema(
        type = "OBJECT",
        properties = mapOf(
            "word" to ResponseSchemaProperty(type = "STRING"),
            "transcription" to ResponseSchemaProperty(type = "STRING"),
            "translation" to ResponseSchemaProperty(type = "STRING"),
            "example" to ResponseSchemaProperty(type = "STRING")
        ),
        required = listOf("word", "transcription", "translation", "example")
    )

    override fun getSessions(): Flow<List<ChatSession>> = sessionDao.getAllSessions().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun createSession(title: String): Long {
        return sessionDao.insertSession(ChatSessionEntity(title = title))
    }

    override suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSession(ChatSessionEntity(id = sessionId, title = ""))
    }

    override suspend fun updateSessionTitle(id: Long, title: String) {
        sessionDao.updateSession(ChatSessionEntity(id = id, title = title))
    }

    override fun getChatHistory(sessionId: Long): Flow<List<ChatMessage>> {
        return chatDao.getMessagesBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun getSystemInstruction(level: String, weaknesses: String) = SystemInstruction(
        parts = listOf(
            Part(
                text = """
                    You are "Buddy", a proactive American English Teacher and Mentor.
                    The student's current English level is $level. Use vocabulary and grammar appropriate for this level.
                    
                    STUDENT WEAKNESSES TO TARGET:
                    $weaknesses
                    Subtly weave situations and practice questions into the conversation to test these specific concepts.
                    
                    Your output MUST be a valid JSON object matching the provided schema.

                    Role & Flow:
                    1. If the student makes a mistake, set hasCorrection to true and provide details.
                    2. If you provide a correction, do NOT repeat the explanation or correction in the tutorResponse. Keep tutorResponse strictly conversational.
                    3. Actively guide the learning lesson.
                    4. CRITICAL: All vocabulary translations in brackets [...] MUST be strictly in UKRAINIAN (Українська мова). Never output Russian words (e.g. use [видаляти], NOT [удалять]).
                    5. Keep tutorResponse brief (1-2 sentences + 1 engaging question).
                    6. Rule 1 (Strict Language Focus): Pivot non-language tasks back to English conversation.
                """.trimIndent()
            )
        )
    )

    override suspend fun sendMessage(sessionId: Long, content: String): Result<Unit> {
        val userMessage = ChatMessage(sessionId = sessionId, content = content, role = MessageRole.USER)
        chatDao.insertMessage(userMessage.toEntity())
        sessionDao.updateSessionTimestamp(sessionId)
        
        trackActivity(ActivityType.MESSAGE)

        val userLevel = userProfileDao.getUserProfile().first()?.cefrLevel ?: "A1"
        
        // Fetch Weaknesses
        val topMistakes = mistakeDao.getTopWeaknesses(5)
        val weaknesses = if (topMistakes.isEmpty()) {
            "None yet. Focus on natural conversation."
        } else {
            topMistakes.joinToString("\n") { "- ${it.originalText} -> ${it.correctedText} (${it.explanation})" }
        }

        return try {
            val history = chatDao.getMessagesBySession(sessionId).first()
            val request = GeminiRequest(
                contents = history.takeLast(15).map { message ->
                    Content(
                        role = if (message.role == "USER") "user" else "model",
                        parts = listOf(Part(text = message.content))
                    )
                },
                systemInstruction = getSystemInstruction(userLevel, weaknesses),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = tutorResponseSchema
                )
            )

            val response = apiService.generateContent(
                model = BuildConfig.GEMINI_MODEL,
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
            val jsonResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonResponse != null) {
                val tutorResponse = json.decodeFromString<TutorResponse>(jsonResponse)
                
                if (tutorResponse.hasCorrection && tutorResponse.errorOriginal != null && tutorResponse.errorCorrected != null) {
                    saveMistake(sessionId, tutorResponse)
                }

                val modelMessage = ChatMessage(sessionId = sessionId, content = jsonResponse, role = MessageRole.MODEL)
                chatDao.insertMessage(modelMessage.toEntity())
                sessionDao.updateSessionTimestamp(sessionId)

                // Smart Title Generation: ONLY after Buddy's first response
                if (history.size == 1) {
                    generateAndSetSessionTitle(sessionId, content)
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("Empty response"))
            }
        } catch (e: HttpException) {
            if (e.code() == 429) {
                Result.failure(Exception("Buddy is a bit overwhelmed. Please wait 10-15 seconds and try again."))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateAndSetSessionTitle(sessionId: Long, firstMessage: String) {
        val request = GeminiRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = "Generate a short (2-4 words) human title for a conversation starting with: '$firstMessage'")))),
            systemInstruction = SystemInstruction(parts = listOf(Part(text = "Return ONLY a plain text title, no JSON, no quotes.")))
        )
        try {
            val response = apiService.generateContent(model = BuildConfig.GEMINI_MODEL, apiKey = BuildConfig.GEMINI_API_KEY, request = request)
            val title = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (!title.isNullOrEmpty()) {
                updateSessionTitle(sessionId, title)
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Title generation failed", e)
        }
    }

    private suspend fun saveMistake(sessionId: Long, response: TutorResponse) {
        val original = response.errorOriginal ?: return
        val corrected = response.errorCorrected ?: return
        val explanation = response.errorExplanationUk ?: ""
        
        val existing = mistakeDao.findMistake(sessionId, original, corrected)
        if (existing != null) {
            mistakeDao.incrementRepeatCount(existing.id)
        } else {
            mistakeDao.insertMistake(MistakeEntity(sessionId = sessionId, originalText = original, correctedText = corrected, explanation = explanation))
        }
    }

    override suspend fun getWordDefinition(word: String): Result<DictionaryResponse> {
        val cached = dictionaryDao.getWord(word)
        if (cached != null) {
            return Result.success(DictionaryResponse(word = cached.word, transcription = cached.transcription, translation = cached.translationUk, example = cached.example))
        }

        val request = GeminiRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = "Define '$word'")))),
            systemInstruction = SystemInstruction(parts = listOf(Part(text = "Return JSON: {word, transcription, translation(UK), example}"))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = dictionaryResponseSchema
            )
        )

        return try {
            val response = apiService.generateContent(model = BuildConfig.GEMINI_MODEL, apiKey = BuildConfig.GEMINI_API_KEY, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                val dictResp = json.decodeFromString<DictionaryResponse>(text)
                dictionaryDao.insertWord(DictionaryEntity(word = dictResp.word, transcription = dictResp.transcription, translationUk = dictResp.translation, example = dictResp.example))
                Result.success(dictResp)
            } else Result.failure(Exception("Not found"))
        } catch (e: Exception) {
            Log.e("ChatRepo", "Dictionary lookup failed for '$word'", e)
            Result.failure(e)
        }
    }

    override fun getSavedWords(): Flow<List<DictionaryEntity>> = dictionaryDao.getSavedWords()
    override suspend fun toggleSaveWord(word: String, isSaved: Boolean) = dictionaryDao.updateSavedStatus(word, isSaved)
    
    override fun getMistakes(sessionId: Long?): Flow<List<MistakeEntity>> {
        val currentTime = System.currentTimeMillis()
        return if (sessionId != null) {
            mistakeDao.getMistakesBySession(sessionId)
        } else {
            mistakeDao.getDueMistakes(currentTime)
        }
    }

    override suspend fun deleteMistake(mistake: MistakeEntity) = mistakeDao.deleteMistake(mistake)

    override suspend fun resolveMistake(mistake: MistakeEntity) {
        trackActivity(ActivityType.MISTAKE_RESOLVED)
        
        val newInterval = when (mistake.intervalDays) {
            0 -> 1
            1 -> 2
            2 -> 4
            4 -> 8
            else -> 8
        }
        
        val nextReview = System.currentTimeMillis() + (newInterval.toLong() * 24 * 60 * 60 * 1000)
        val isMastered = newInterval >= 8
        
        mistakeDao.updateMistake(mistake.copy(
            intervalDays = newInterval,
            nextReviewTimestamp = nextReview,
            isMastered = isMastered
        ))
    }

    override fun getUserProfile(): Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    override suspend fun updateUserProfile(profile: UserProfileEntity) = userProfileDao.updateProfile(profile)

    override fun getActivity(): Flow<List<DailyActivityEntity>> = activityDao.getAllActivity()
    
    override suspend fun trackActivity(type: ActivityType) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(Date())
        val existing = activityDao.getActivity(date)
        if (existing == null) {
            activityDao.insertActivity(DailyActivityEntity(date = date))
            updateStreak(date)
        }
        when (type) {
            ActivityType.MINUTE -> activityDao.incrementMinutes(date)
            ActivityType.MESSAGE -> activityDao.incrementMessages(date)
            ActivityType.MISTAKE_RESOLVED -> activityDao.incrementMistakes(date)
        }
    }

    private suspend fun updateStreak(currentDate: String) {
        val profile = userProfileDao.getUserProfile().first() ?: return
        val lastDate = profile.lastActiveDate
        
        if (lastDate.isEmpty()) {
            userProfileDao.updateProfile(profile.copy(currentStreak = 1, lastActiveDate = currentDate))
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val current = sdf.parse(currentDate)
        val last = sdf.parse(lastDate)
        
        if (current == null || last == null) return
        
        val diff = (current.time - last.time) / (1000 * 60 * 60 * 24)
        
        val newStreak = when (diff) {
            1L -> profile.currentStreak + 1
            0L -> profile.currentStreak
            else -> 1
        }
        
        userProfileDao.updateProfile(profile.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(newStreak, profile.longestStreak),
            lastActiveDate = currentDate
        ))
    }

    override suspend fun clearHistory(sessionId: Long) = chatDao.clearHistoryBySession(sessionId)
}
