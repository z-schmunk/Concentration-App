package com.example.concentrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.concentrate.data.ChatViewModel
import com.example.concentrate.ui.screens.AIChatScreen
import com.example.concentrate.ui.screens.QuizScreen
import com.example.concentrate.ui.screens.WelcomeScreen
import com.example.concentrate.ui.theme.ConcentrateTheme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Welcome : Screen("welcome", "Welcome", Icons.Default.Home)
    object Quiz : Screen("quiz", "Quiz", Icons.Default.Quiz)
    object AIChat : Screen("aichat", "AI Chat", Icons.Default.Chat)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConcentrateTheme {
                val navController = rememberNavController()
                val chatViewModel: ChatViewModel = viewModel()
                
                val items = listOf(
                    Screen.Welcome,
                    Screen.Quiz,
                    Screen.AIChat
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                        navController,
                        startDestination = Screen.Welcome.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Welcome.route) { WelcomeScreen() }
                        composable(Screen.Quiz.route) { 
                            QuizScreen(
                                onNavigateToChat = { major ->
                                    chatViewModel.sendMessage("Tell me more about the $major major at ONU.", applicationContext)
                                    navController.navigate(Screen.AIChat.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            ) 
                        }
                        composable(Screen.AIChat.route) { 
                            AIChatScreen(viewModel = chatViewModel) 
                        }
                    }
                }
            }
        }
    }
}