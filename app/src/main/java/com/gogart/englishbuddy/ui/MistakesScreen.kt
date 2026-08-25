package com.gogart.englishbuddy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gogart.englishbuddy.data.local.entity.MistakeEntity
import com.gogart.englishbuddy.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakesScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mistake Notebook") })
        }
    ) { padding ->
        if (uiState.allMistakes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No reviews due today!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        "You've cleared your notebook. Great job!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.allMistakes, key = { it.id }) { mistake ->
                    MistakeCard(
                        mistake = mistake,
                        onDelete = { viewModel.deleteMistake(it) },
                        onValidate = { viewModel.validateMistake(mistake, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun MistakeCard(
    mistake: MistakeEntity,
    onDelete: (MistakeEntity) -> Unit,
    onValidate: (String) -> Boolean
) {
    var practiceText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<ValidationStatus>(ValidationStatus.Idle) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(status) {
                ValidationStatus.Correct -> Color(0xFFE8F5E9)
                ValidationStatus.Incorrect -> Color(0xFFFFEBEE)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.error)) {
                            append(mistake.originalText)
                        }
                        append(" ➔ ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append(mistake.correctedText)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { onDelete(mistake) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            if (mistake.explanation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mistake.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (status == ValidationStatus.Correct) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(Modifier.width(8.dp))
                    Text("🎉 Resolved!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = practiceText,
                    onValueChange = { 
                        practiceText = it
                        if (status == ValidationStatus.Incorrect) status = ValidationStatus.Idle
                    },
                    label = { Text("Rewrite correctly") },
                    isError = status == ValidationStatus.Incorrect,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { 
                            val isCorrect = onValidate(practiceText)
                            status = if (isCorrect) ValidationStatus.Correct else ValidationStatus.Incorrect
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Check")
                        }
                    }
                )
                if (status == ValidationStatus.Incorrect) {
                    Text("Almost! Try again.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

enum class ValidationStatus { Idle, Correct, Incorrect }
