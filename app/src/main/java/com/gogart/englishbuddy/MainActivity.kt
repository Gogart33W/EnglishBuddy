package com.gogart.englishbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.gogart.englishbuddy.data.local.AppDatabase
import com.gogart.englishbuddy.data.remote.NetworkClient
import com.gogart.englishbuddy.data.repository.ChatRepositoryImpl
import com.gogart.englishbuddy.ui.ChatScreen
import com.gogart.englishbuddy.ui.theme.EnglishBuddyTheme
import com.gogart.englishbuddy.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "english_buddy.db"
                ).build()
                val repository = ChatRepositoryImpl(NetworkClient.geminiApiService, db.chatMessageDao)
                return ChatViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnglishBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}
