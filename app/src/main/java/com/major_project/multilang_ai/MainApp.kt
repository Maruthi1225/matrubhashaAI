package com.major_project.multilang_ai

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.major_project.multilang_ai.ui.HomeScreen
import com.major_project.multilang_ai.ui.SettingsScreen

@Composable
fun MainApp(gemini: GeminiService, voice: VoiceManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(gemini = gemini, voice = voice, navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}
