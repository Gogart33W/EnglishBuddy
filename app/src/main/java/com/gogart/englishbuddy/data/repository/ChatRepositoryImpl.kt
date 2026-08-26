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
import com.gogart.englishbuddy.util.AppConfig
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
                    You are "Buddy", a proactive American English Teacher.
                    Level: $level. WEAKNESSES: $weaknesses.
                    
                    Rules:
                    1. Output strictly valid JSON per schema.
                    2. If error exists, set hasCorrection:true.
                    3. No repetition of correction in tutorResponse.
                    4. UKRAINIAN translations in [] for tough words.
                    5. Brief tutorResponse (1-2 sentences + question).
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
        
        val topMistakes = mistakeDao.getTopWeaknesses(3) // Pruned to top 3
        val weaknesses = if (topMistakes.isEmpty()) "None" else topMistakes.joinToString(", ") { "${it.originalText}->${it.correctedText}" }

        return try {
            val history = chatDao.getMessagesBySession(sessionId).first()
            
            if (history.size == 1) {
                generateLocalTitle(sessionId, content)
            }

            val request = GeminiRequest(
                contents = history.takeLast(6).map { message -> // Pruned context to 6
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
                model = AppConfig.MODEL_NAME,
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

                Result.success(Unit)
            } else {
                Result.failure(Exception("Empty response"))
            }
        } catch (e: HttpException) {
            if (e.code() == 429) {
                Result.failure(Exception("Buddy is busy. Please wait a few seconds."))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateLocalTitle(sessionId: Long, firstMessage: String) {
        val words = firstMessage.trim().split(Regex("\\s+"))
            .take(4)
            .joinToString(" ") { it.replace(Regex("[^a-zA-Z0-9]"), "") }
            .replaceFirstChar { it.uppercase() }
        
        if (words.isNotEmpty()) {
            updateSessionTitle(sessionId, if (words.length > 25) words.take(22) + "..." else words)
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
            val response = apiService.generateContent(model = AppConfig.MODEL_NAME, request = request)
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
