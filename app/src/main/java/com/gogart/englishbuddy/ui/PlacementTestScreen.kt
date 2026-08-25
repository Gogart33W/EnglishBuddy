package com.gogart.englishbuddy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gogart.englishbuddy.viewmodel.ChatViewModel

data class Question(val text: String, val options: List<String>, val correctIndex: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementTestScreen(viewModel: ChatViewModel, onComplete: () -> Unit) {
    val questions = remember {
        listOf(
            Question("I ___ to the gym every day.", listOf("go", "goes", "going", "gone"), 0),
            Question("She ___ English for five years.", listOf("is studying", "has been studying", "studies", "studied"), 1),
            Question("If I ___ you, I would take the job.", listOf("am", "was", "were", "be"), 2),
            Question("By next year, I ___ my degree.", listOf("will finish", "will have finished", "finish", "am finishing"), 1),
            Question("Hardly ___ the station when the train left.", listOf("I had reached", "had I reached", "I reached", "did I reach"), 1),
            Question("I'm looking forward ___ you.", listOf("to see", "seeing", "to seeing", "see"), 2),
            Question("The car ___ by the time we arrived.", listOf("was repaired", "had been repaired", "repaired", "has been repaired"), 1),
            Question("I wish I ___ so much cake.", listOf("didn't eat", "hadn't eaten", "won't eat", "don't eat"), 1),
            Question("___ of the two candidates is suitable.", listOf("Neither", "None", "No one", "Not any"), 0),
            Question("It's high time you ___ home.", listOf("go", "went", "gone", "going"), 1)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Level Placement Test") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!finished) {
                val q = questions[currentIndex]
                LinearProgressIndicator(progress = (currentIndex + 1).toFloat() / questions.size, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(32.dp))
                Text("Question ${currentIndex + 1}/${questions.size}", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(16.dp))
                Text(q.text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(32.dp))
                q.options.forEachIndexed { index, option ->
                    Button(
                        onClick = {
                            if (index == q.correctIndex) score++
                            if (currentIndex < questions.size - 1) currentIndex++ else finished = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) { Text(option) }
                }
            } else {
                val level = when {
                    score >= 9 -> "C1"
                    score >= 7 -> "B2"
                    score >= 5 -> "B1"
                    score >= 3 -> "A2"
                    else -> "A1"
                }
                Text("Test Completed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Your estimated level is", style = MaterialTheme.typography.bodyLarge)
                Text(level, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(48.dp))
                Button(onClick = { 
                    viewModel.updateLevel(level)
                    onComplete()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Apply Level & Start Learning")
                }
            }
        }
    }
}
