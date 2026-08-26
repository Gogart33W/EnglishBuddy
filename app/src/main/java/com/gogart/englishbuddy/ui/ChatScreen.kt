package com.gogart.englishbuddy.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.ChatSession
import com.gogart.englishbuddy.domain.model.MessageRole
import com.gogart.englishbuddy.data.remote.dto.TutorResponse
import com.gogart.englishbuddy.ui.util.MarkdownText
import com.gogart.englishbuddy.ui.util.TtsManager
import com.gogart.englishbuddy.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val ttsManager = remember { TtsManager(context) }
    var activeSpeakingMessageId by remember { mutableLongStateOf(-1L) }

    DisposableEffect(Unit) {
        onDispose { ttsManager.release() }
    }

    // Stop TTS when switching sessions
    LaunchedEffect(uiState.currentSessionId) {
        ttsManager.stop()
        activeSpeakingMessageId = -1L
    }

    // Sync active message ID with TTS state
    LaunchedEffect(ttsManager.isSpeaking) {
        if (!ttsManager.isSpeaking) {
            activeSpeakingMessageId = -1L
        }
    }

    var selectedWord by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "EnglishBuddy History",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                NavigationDrawerItem(
                    label = { Text("New Conversation") },
                    selected = false,
                    onClick = {
                        viewModel.createNewSession()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                uiState.sessions.forEach { session ->
                    val isSelected = uiState.currentSessionId == session.id
                    NavigationDrawerItem(
                        label = { 
                            Column {
                                Text(
                                    session.title,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                val date = remember(session.updatedAt) {
                                    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(session.updatedAt))
                                }
                                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        },
                        selected = isSelected,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            viewModel.switchSession(session.id)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Face, contentDescription = null) },
                        badge = {
                            IconButton(onClick = { viewModel.deleteSession(session.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Delete Session", modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .padding(2.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Buddy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Active now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            bottomBar = {
                ChatInputBar(
                    isLoading = uiState.isLoading,
                    onSendMessage = { viewModel.sendMessage(it) }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    EmptyState(onTopicClick = { viewModel.sendMessage(it) })
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatBubble(
                                message = message,
                                isSpeaking = activeSpeakingMessageId == message.id,
                                onWordClick = { word ->
                                    if (!uiState.isDictionaryLoading) {
                                        selectedWord = word
                                        viewModel.fetchWordDefinition(word)
                                        showSheet = true
                                    }
                                },
                                onToggleSpeak = { text ->
                                    if (activeSpeakingMessageId == message.id) {
                                        ttsManager.stop()
                                        activeSpeakingMessageId = -1L
                                    } else {
                                        ttsManager.speak(text)
                                        activeSpeakingMessageId = message.id
                                    }
                                }
                            )
                        }

                        if (uiState.isLoading) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }

                if (showSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSheet = false },
                        sheetState = sheetState
                    ) {
                        WordDetailContent(
                            word = selectedWord ?: "",
                            definition = uiState.wordDefinition,
                            isLoading = uiState.isDictionaryLoading,
                            isSaved = uiState.savedWords.any { it.word == selectedWord },
                            onToggleSave = { viewModel.toggleWordSaved(it, !uiState.savedWords.any { w -> w.word == it }) },
                            onPronounce = { ttsManager.speak(it) }
                        )
                    }
                }

                uiState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(text = error)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(onTopicClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to EnglishBuddy!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Your American friend is ready to chat. Pick a topic to start!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        val topics = listOf(
            "Let's talk about coding 💻",
            "Recommend a movie 🎬",
            "Grammar check 📝"
        )
        
        topics.forEach { topic ->
            SuggestionChip(
                onClick = { onTopicClick(topic) },
                label = { Text(topic) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onWordClick: (String) -> Unit,
    onToggleSpeak: (String) -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        val tutorResponse = remember(message.content) {
            if (!isUser) {
                try {
                    Json.decodeFromString<TutorResponse>(message.content)
                } catch (e: Exception) {
                    null
                }
            } else null
        }

        tutorResponse?.let { resp ->
            if (resp.hasCorrection) {
                CorrectionCard(
                    original = resp.errorOriginal ?: "",
                    corrected = resp.errorCorrected ?: "",
                    explanation = resp.errorExplanationUk ?: ""
                )
            }
        }

        val mainText = tutorResponse?.tutorResponse ?: message.content

        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (isUser) {
                    Text(
                        text = mainText,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                    )
                } else {
                    InteractiveText(
                        text = mainText,
                        onWordClick = onWordClick,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                    )
                    
                    tutorResponse?.practicePrompt?.let { prompt ->
                        Spacer(modifier = Modifier.height(8.dp))
                        MarkdownText(
                            text = "✍️ Practice: $prompt",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            ),
                            color = contentColor.copy(alpha = 0.9f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onToggleSpeak(mainText) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = if (isSpeaking) "Stop" else "Speak",
                                modifier = Modifier.size(18.dp),
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CorrectionCard(original: String, corrected: String, explanation: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .widthIn(max = 280.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "💡 Correction",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)) {
                        append(original)
                    }
                    append(" ➔ ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(corrected)
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (explanation.isNotEmpty()) {
                Text(
                    text = "($explanation)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun InteractiveText(
    text: String,
    onWordClick: (String) -> Unit,
    style: androidx.compose.ui.text.TextStyle
) {
    val words = remember(text) { text.split(Regex("(?<=\\s)|(?=\\s)")).filter { it.isNotEmpty() } }
    
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        words.forEach { word ->
            if (word.isBlank()) {
                Text(text = word, style = style)
            } else {
                val cleanWord = word.trim().filter { it.isLetterOrDigit() }
                Text(
                    text = word,
                    modifier = Modifier
                        .background(
                            if (cleanWord.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                            else Color.Transparent, 
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { 
                            if (cleanWord.isNotEmpty()) onWordClick(cleanWord) 
                        },
                    style = style
                )
            }
        }
    }
}

@Composable
fun WordDetailContent(
    word: String,
    definition: com.gogart.englishbuddy.data.remote.dto.DictionaryResponse?,
    isLoading: Boolean,
    isSaved: Boolean,
    onToggleSave: (String) -> Unit,
    onPronounce: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onToggleSave(word) }) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "Save Word",
                    tint = if (isSaved) Color(0xFFFFC107) else Color.Gray
                )
            }
        }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else if (definition != null) {
            Text(
                text = definition.transcription,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = definition.translation,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Example: \"${definition.example}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onPronounce(word) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pronounce")
            }
            
            OutlinedButton(
                onClick = { /* Dictionary API */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Dictionary")
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "typing")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_alpha"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colorScheme.onSecondaryContainer, CircleShape)
            )
        }
    }
}

@Composable
fun ChatInputBar(
    isLoading: Boolean,
    onSendMessage: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = { Text("Ask Buddy something...") },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                maxLines = 5,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(12.dp))
            val gradientBrush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            )
            
            val isEnabled = text.isNotBlank() && !isLoading
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) gradientBrush else Brush.linearGradient(listOf(Color.LightGray, Color.LightGray)))
                    .clickable(enabled = isEnabled) {
                        onSendMessage(text)
                        text = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
