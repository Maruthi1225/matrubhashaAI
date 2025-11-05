package com.major_project.multilang_ai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.major_project.multilang_ai.google.GeminiService
import com.major_project.multilang_ai.R
import com.major_project.multilang_ai.ui.theme.AppThemeColors
import com.major_project.multilang_ai.uiNav.LanguageDropdown
import com.major_project.multilang_ai.uiNav.UserPreferences
import com.major_project.multilang_ai.uiNav.VoiceOnlyChatScreen
import com.major_project.multilang_ai.voice.AppLanguage
import com.major_project.multilang_ai.voice.VoiceManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    gemini: GeminiService,
    voice: VoiceManager,
    navController: NavHostController
) {
    var selectedLanguage by remember { mutableStateOf(UserPreferences.getLanguage(navController.context)) }
    val coroutineScope = rememberCoroutineScope()
    val backgroundGradient = AppThemeColors.backgroundGradient()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Matrubhasha AI",
                        color = AppThemeColors.textColorPrimary(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    LanguageDropdown(
                        selectedLanguage = selectedLanguage,
                        onLanguageChange = {
                            selectedLanguage = it
                            UserPreferences.setLanguage(navController.context, it)
                        }
                    )

                    IconButton(onClick = { navController.navigate("settings") }) {
                        Image(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = AppThemeColors.textColorPrimary(),

                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(backgroundGradient)
    ) { paddingValues ->

        // 🌌 Voice chat section
        VoiceOnlyChatScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues),
            onVoiceInput = { onRecognized ->
                coroutineScope.launch {
                    voice.listen(
                        onResult = { text, _ ->
                            coroutineScope.launch {
                                voice.pauseTTS()
                                val reply = gemini.getResponse(text)
                                onRecognized(text)
                                voice.speakInstant(reply, selectedLanguage)
                            }
                        },
                        onError = { println("Voice Error: $it") }
                    )
                }
            },
            onSendMessage = { userText, onResponse ->
                coroutineScope.launch {
                    voice.pauseTTS()
                    val reply = gemini.getResponse(userText)
                    onResponse(reply)
                    voice.speakInstant(reply, selectedLanguage)
                }
            },
            voice = voice,
            selectedLanguage = selectedLanguage
        )
    }
}

