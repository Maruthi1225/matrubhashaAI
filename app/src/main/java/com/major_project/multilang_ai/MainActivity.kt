package com.major_project.multilang_ai


import VoiceManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.BuildConfig

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var gemini: GeminiService
    private lateinit var voice: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = Secrets.GEMINI_API_KEY
        gemini = GeminiService(apiKey)
        voice = VoiceManager(this)

        setContent {
            VoiceOnlyChatScreen(
                onVoiceInput = { onResult ->
                    voice.listen(
                        onResult = { spokenText ->
                            lifecycleScope.launch {
                                val lang = gemini.detectLanguage(spokenText)
                                voice.setLanguage(lang)
                                onResult(spokenText)
                            }
                        },
                        onError = { println("Voice input error: $it") }
                    )
                },
                onSendMessage = { userText, onResponse ->
                    lifecycleScope.launch {
                        val reply = gemini.getResponse(userText)
                        voice.speak(reply)
                        onResponse(reply)
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voice.release()
    }
}
@Composable
fun VoiceOnlyChatScreen(
    onVoiceInput: ((String) -> Unit) -> Unit,
    onSendMessage: (String, (String) -> Unit) -> Unit
) {
    var messages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var isListening by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "🎙️ Groot Voice AI Assistant",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1A237E)
        )

        Spacer(Modifier.height(12.dp))

        // Chat history
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                ChatBubble(msg.first, msg.second)
            }
        }

        Spacer(Modifier.height(12.dp))

        // 🎤 Mic Button (Voice Trigger)
        Button(
            onClick = {
                isListening = true
                onVoiceInput { spokenText ->
                    isListening = false
                    if (spokenText.isNotEmpty()) {
                        messages = messages + ("You" to spokenText)

                        onSendMessage(spokenText) { reply ->
                            messages = messages + ("Groot" to reply)
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) Color.Red else Color(0xFF3949AB)
            ),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.size(120.dp)
        ) {
            Text(if (isListening) "Listening..." else "Tap to Speak", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))
    }
}


@Composable
fun ChatBubble(sender: String, message: String) {
    val isUser = sender == "You"
    val bubbleColor = if (isUser) Color(0xFFDCF8C6) else Color(0xFFE8E8E8)
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment as Alignment
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
                .widthIn(0.dp, 280.dp)
        ) {
            Text("$sender: $message")
        }
    }
    Spacer(Modifier.height(8.dp))
}
