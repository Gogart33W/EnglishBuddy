package com.gogart.englishbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogart.englishbuddy.domain.repository.ChatRepository
import com.gogart.englishbuddy.domain.repository.ActivityType
import com.gogart.englishbuddy.ui.ChatUiState
import com.gogart.englishbuddy.data.local.entity.UserProfileEntity
import com.gogart.englishbuddy.data.local.entity.MistakeEntity
import com.gogart.englishbuddy.util.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var historyJob: Job? = null
    private var activityJob: Job? = null

    init {
        // Observe Sessions
        viewModelScope.launch {
            repository.getSessions().collect { sessions ->
                _uiState.update { it.copy(sessions = sessions) }
                
                if (_uiState.value.currentSessionId == null) {
                    val lastId = prefs.lastSessionId.first()
                    if (lastId != null && sessions.any { it.id == lastId }) {
                        switchSession(lastId)
                    } else if (sessions.isNotEmpty()) {
                        switchSession(sessions.first().id)
                    } else {
                        createNewSession()
                    }
                }
            }
        }

        // Observe Saved Words, Mistakes, Profile, Activity
        viewModelScope.launch {
            merge(
                repository.getSavedWords().onEach { words -> _uiState.update { it.copy(savedWords = words) } },
                repository.getMistakes().onEach { mistakes -> _uiState.update { it.copy(allMistakes = mistakes) } },
                repository.getUserProfile().onEach { profile -> 
                    if (profile == null) repository.updateUserProfile(UserProfileEntity())
                    else _uiState.update { it.copy(userProfile = profile) }
                },
                repository.getActivity().onEach { activity -> _uiState.update { it.copy(dailyActivity = activity) } }
            ).collect()
        }

        startActivityTracking()
    }

    private fun startActivityTracking() {
        activityJob?.cancel()
        activityJob = viewModelScope.launch {
            while (true) {
                delay(60000) // 1 minute
                repository.trackActivity(ActivityType.MINUTE)
            }
        }
    }

    fun switchSession(sessionId: Long) {
        if (_uiState.value.currentSessionId == sessionId) return
        
        historyJob?.cancel()
        _uiState.update { it.copy(currentSessionId = sessionId, messages = emptyList()) }
        
        viewModelScope.launch { prefs.saveLastSessionId(sessionId) }

        historyJob = viewModelScope.launch {
            repository.getChatHistory(sessionId).collect { history ->
                _uiState.update { it.copy(messages = history) }
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val id = repository.createSession("New Conversation")
            switchSession(id)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                _uiState.update { it.copy(currentSessionId = null, messages = emptyList()) }
            }
        }
    }

    fun deleteMistake(mistake: MistakeEntity) {
        viewModelScope.launch { repository.deleteMistake(mistake) }
    }

    fun validateMistake(mistake: MistakeEntity, input: String): Boolean {
        val normalizedInput = input.trim().lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "")
        val normalizedCorrect = mistake.correctedText.trim().lowercase().replace(Regex("[^a-zA-Z0-9 ]"), "")
        
        return if (normalizedInput == normalizedCorrect) {
            viewModelScope.launch { repository.resolveMistake(mistake) }
            true
        } else false
    }

    fun toggleWordSaved(word: String, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSaveWord(word, isSaved)
        }
    }

    fun updateLevel(level: String) {
        viewModelScope.launch {
            val current = _uiState.value.userProfile ?: return@launch
            repository.updateUserProfile(current.copy(cefrLevel = level))
        }
    }

    fun clearChat() {
        val sessionId = _uiState.value.currentSessionId ?: return
        viewModelScope.launch { repository.clearHistory(sessionId) }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.isLoading) return
        val sessionId = _uiState.value.currentSessionId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.sendMessage(sessionId, text)
            _uiState.update { state ->
                state.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun fetchWordDefinition(word: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDictionaryLoading = true, wordDefinition = null) }
            val result = repository.getWordDefinition(word)
            _uiState.update { state ->
                state.copy(isDictionaryLoading = false, wordDefinition = result.getOrNull())
            }
        }
    }
}
