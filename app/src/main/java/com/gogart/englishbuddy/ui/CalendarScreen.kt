package com.gogart.englishbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gogart.englishbuddy.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile
    
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val daySdf = remember { SimpleDateFormat("d", Locale.getDefault()) }
    
    val days = remember {
        val list = mutableListOf<Calendar>()
        for (i in 0..29) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            list.add(cal)
        }
        list.reversed()
    }
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("Activity Calendar") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Streak Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Current Streak", style = MaterialTheme.typography.labelLarge)
                        Text("${profile?.currentStreak ?: 0} Days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text("Last 30 Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            
            LazyVerticalGrid(columns = GridCells.Adaptive(40.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(days) { cal ->
                    val dateStr = sdf.format(cal.time)
                    val activity = uiState.dailyActivity.find { it.date == dateStr }
                    ActivityCell(daySdf.format(cal.time), activity?.activeMinutes ?: 0)
                }
            }
        }
    }
}

@Composable
fun ActivityCell(day: String, minutes: Int) {
    val color = when {
        minutes >= 15 -> Color(0xFF4CAF50)
        minutes >= 5 -> Color(0xFF81C784)
        minutes > 0 -> Color(0xFFC8E6C9)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day, style = MaterialTheme.typography.labelSmall, color = if (minutes > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
