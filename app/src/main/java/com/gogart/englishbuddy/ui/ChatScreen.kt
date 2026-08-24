package com.gogart.englishbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gogart.englishbuddy.domain.model.ChatMessage
import com.gogart.englishbuddy.domain.model.MessageRole
import com.gogart.englishbuddy.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scrollState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        bottomBar = {
            ChatInputBar(
                isLoading = uiState.isLoading,
                onSendMessage = { viewModel.sendMessage(it) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(message = message)
                }

                if (uiState.isLoading) {
                    item {
                        Text(
                            text = "Buddy is typing...",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(containerColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
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
        tonalElevation = 2.dp,
        modifier = Modifier.imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message Buddy...") },
                maxLines = 4,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    onSendMessage(text)
                    text = ""
                },
                enabled = !isLoading && text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}
