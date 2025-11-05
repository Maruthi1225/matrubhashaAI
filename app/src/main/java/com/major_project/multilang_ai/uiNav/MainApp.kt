package com.major_project.multilang_ai.uiNav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.major_project.multilang_ai.google.GeminiService
import com.major_project.multilang_ai.ui.HomeScreen
import com.major_project.multilang_ai.voice.VoiceManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.major_project.multilang_ai.ui.theme.AppTheme


@Composable
fun MainApp(gemini: GeminiService, voice: VoiceManager) {
    val ctx = LocalContext.current

    var darkTheme by remember { mutableStateOf(UserPreferences.isDarkTheme(ctx)) }

    AppTheme(darkTheme = darkTheme) { // apply app-level theme
        val navController = rememberNavController()

        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(gemini = gemini, voice = voice, navController = navController)
            }
            composable("settings") {
                // Pass a lambda so SettingsScreen toggles the root theme state
                SettingsScreen(
                    navController = navController,
                    voice = voice,
                    onThemeChange = { enabled ->
                        darkTheme = enabled
                        UserPreferences.setDarkTheme(ctx, enabled) // persist
                    }
                )
            }
        }
    }
}