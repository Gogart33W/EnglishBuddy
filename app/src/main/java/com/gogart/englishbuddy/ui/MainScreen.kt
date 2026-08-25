package com.gogart.englishbuddy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gogart.englishbuddy.viewmodel.ChatViewModel

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Chat : Screen("chat", "Chat", Icons.AutoMirrored.Filled.Chat)
    object Vocabulary : Screen("vocabulary", "Vocabulary", Icons.Default.Book)
    object Mistakes : Screen("mistakes", "Mistakes", Icons.Default.Error)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object PlacementTest : Screen("placement_test", "Test", Icons.Default.Person)
}

@Composable
fun MainScreen(viewModel: ChatViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Chat,
        Screen.Vocabulary,
        Screen.Mistakes,
        Screen.Calendar,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) { ChatScreen(viewModel) }
            composable(Screen.Vocabulary.route) { VocabularyScreen(viewModel) }
            composable(Screen.Mistakes.route) { MistakesScreen(viewModel) }
            composable(Screen.Calendar.route) { CalendarScreen(viewModel) }
            composable(Screen.Profile.route) { ProfileScreen(viewModel, onStartTest = { navController.navigate(Screen.PlacementTest.route) }) }
            composable(Screen.PlacementTest.route) { PlacementTestScreen(viewModel, onComplete = { navController.popBackStack() }) }
        }
    }
}
