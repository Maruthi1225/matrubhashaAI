package com.major_project.multilang_ai.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.major_project.multilang_ai.VoiceManager
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceOnlyChatScreen(
    modifier: Modifier = Modifier,
    onVoiceInput: (onRecognized: (String) -> Unit) -> Unit,
    onSendMessage: (String, onResponse: (String) -> Unit) -> Unit,
    voice: VoiceManager,
    selectedLanguage: String
) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val coroutineScope = rememberCoroutineScope()
    var isListening by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            MicButton(
                isListening = isListening,
                onClick = {
                    isListening = true
                    onVoiceInput { recognizedText ->
                        isListening = false
                        messages.add(ChatMessage(recognizedText, true))
                        coroutineScope.launch {
                            onSendMessage(recognizedText) { reply ->
                                messages.add(ChatMessage(reply, false))
                                // 🔊 Speak instantly when message received
                                voice.speakInstant(reply, selectedLanguage)
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .clickable { voice.pauseTTS() } // 👈 Tap empty space to stop speech
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    ChatBubble(
                        message = msg,
                        onBubbleClick = {
                            if (!msg.isUser) {
                                voice.resumeTTS(msg.text, selectedLanguage) // 👈 Resume if bot msg
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onBubbleClick: () -> Unit) {
    val isUserMessage = message.isUser
    val bubbleBrush = if (isUserMessage)
        Brush.horizontalGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
    else
        Brush.horizontalGradient(listOf(Color(0xFF232526), Color(0xFF414345)))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUserMessage) 18.dp else 4.dp,
                bottomEnd = if (isUserMessage) 4.dp else 18.dp
            ),
            color = Color.Transparent,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .background(bubbleBrush)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 300.dp)
                    .clickable(enabled = !isUserMessage) { onBubbleClick() } // 👈 Resume only for AI bubbles
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}


@Composable
fun MicButton(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(if (isListening) (110 * pulse).dp else 90.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) Color(0xFF00E676) else Color(0xFF1E88E5)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (isListening) "🎧 Listening..." else "🎙️ Tap to Speak",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}
