package com.major_project.multilang_ai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.major_project.multilang_ai.R
import com.major_project.multilang_ai.sarvam.LocalAIService
import com.major_project.multilang_ai.ui.theme.AppThemeColors
import com.major_project.multilang_ai.uiNav.LanguageDropdown
import com.major_project.multilang_ai.uiNav.UserPreferences
import com.major_project.multilang_ai.uiNav.VoiceOnlyChatScreen
import com.major_project.multilang_ai.voice.VoiceManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    localAI: LocalAIService,
    voice: VoiceManager,
    navController: NavHostController
) {
    var selectedLanguage by remember { mutableStateOf(UserPreferences.getLanguage(navController.context)) }
    val coroutineScope = rememberCoroutineScope()
    val backgroundGradient = AppThemeColors.backgroundGradient()

    val isAiReady by localAI.isReady

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

        if (!isAiReady) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.Cyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Matrubhasha AI is waking up...",
                        color = AppThemeColors.textColorPrimary(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            VoiceOnlyChatScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(paddingValues),
                onVoiceInput = { onRecognized ->
                    coroutineScope.launch {
                        voice.listen(
                            onResult = { text, langCode ->
                                coroutineScope.launch {
                                    voice.pauseTTS()
                                    // Clean: Just pass the text, let Service handle the prompt
                                    val reply = localAI.getResponse(text, langCode)
                                    onRecognized(text)
                                    voice.speakInstant(reply, langCode)
                                }
                            },
                            onError = { println("Voice Error: $it") }
                        )
                    }
                },
                onSendMessage = { userText, onResponse ->
                    coroutineScope.launch {
                        voice.pauseTTS()
                        val prompt = """
                                         You are Matrubhasha AI, a multilingual Indian assistant.
                                         User Language: $selectedLanguage
                                         Task: Answer the user's question briefly and strictly ONLY in $selectedLanguage script.
                                         Do not provide any English translation or explanation.
                                         
                                         User: ${userText}
                                         Assistant ($selectedLanguage):
                                     """.trimIndent()

                        val reply = localAI.getResponse(prompt,selectedLanguage)
                        onResponse(reply)
                        voice.speakInstant(reply, selectedLanguage)
                    }
                },
                voice = voice,
                selectedLanguage = selectedLanguage
            )
        }
    }
}