package com.major_project.multilang_ai.uiNav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.major_project.multilang_ai.sarvam.LocalAIService
import com.major_project.multilang_ai.uiNav.HomeScreen
import com.major_project.multilang_ai.ui.theme.AppTheme
import com.major_project.multilang_ai.voice.VoiceManager

@Composable
fun MainApp(localAI: LocalAIService, voice: VoiceManager) {
    val ctx = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    
    // Track user authentication state
    var user by remember { mutableStateOf(auth.currentUser) }
    var darkTheme by remember { mutableStateOf(UserPreferences.isDarkTheme(ctx)) }

    AppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = if (user == null) "login" else "home"
        ) {
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    user = auth.currentUser
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            
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