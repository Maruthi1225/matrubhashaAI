package com.major_project.multilang_ai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.major_project.multilang_ai.GeminiService
import com.major_project.multilang_ai.R
import com.major_project.multilang_ai.UserPreferences
import com.major_project.multilang_ai.VoiceManager
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

    Column {
        CenterAlignedTopAppBar(
            title = { Text("Matrubhasha AI") },
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
            }
        )

        // Chat section (no delays, direct processing)
        VoiceOnlyChatScreen(
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
            voice = voice, // 👈 added
            selectedLanguage = selectedLanguage // 👈 added
        )
    }
}
