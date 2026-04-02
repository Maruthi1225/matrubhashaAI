package com.major_project.multilang_ai.uiNav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
//import com.major_project.multilang_ai.google.GeminiService
import com.major_project.multilang_ai.ui.HomeScreen
import com.major_project.multilang_ai.voice.VoiceManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.major_project.multilang_ai.sarvam.LocalAIService
import com.major_project.multilang_ai.ui.theme.AppTheme


@Composable
fun MainApp(
    //gemini: GeminiService,
    localAI: LocalAIService, voice: VoiceManager) {
    val ctx = LocalContext.current

    var darkTheme by remember { mutableStateOf(UserPreferences.isDarkTheme(ctx)) }

    AppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = "home"
        ) {
//            composable("home") {
//                HomeScreen(gemini = gemini, voice = voice, navController = navController)
//            }
            composable("home") {
                HomeScreen(localAI = localAI, voice = voice, navController = navController)
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    voice = voice,
                    onThemeChange = { enabled ->
                        darkTheme = enabled
                        UserPreferences.setDarkTheme(ctx, enabled)
                    }
                )
            }
        }
    }
}