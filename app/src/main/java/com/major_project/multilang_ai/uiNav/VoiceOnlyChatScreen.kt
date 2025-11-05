package com.major_project.multilang_ai.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        modifier = modifier.background(gradient),
        containerColor = Color.Transparent,
        bottomBar = {
            ModernMicButton(
                isListening = isListening,
                onClick = {
                    isListening = true
                    onVoiceInput { recognizedText ->
                        isListening = false
                        messages.add(ChatMessage(recognizedText, true))
                        coroutineScope.launch {
                            onSendMessage(recognizedText) { reply ->
                                messages.add(ChatMessage(reply, false))
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
                .clickable { voice.pauseTTS() } // stop TTS when tapping background
        ) {
            if (messages.isEmpty()) {
                EmptyChatPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { msg ->
                        ChatBubble(
                            message = msg,
                            onBubbleClick = {
                                if (!msg.isUser) voice.resumeTTS(msg.text, selectedLanguage)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎙️ Speak to Matrubhasha AI",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your multilingual voice assistant is listening...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onBubbleClick: () -> Unit) {
    val isUserMessage = message.isUser
    val bubbleBrush = if (isUserMessage)
        Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
    else
        Brush.linearGradient(listOf(Color(0xFF434343), Color(0xFF000000)))

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUserMessage) 18.dp else 4.dp,
                        bottomEnd = if (isUserMessage) 4.dp else 18.dp
                    )
                )
                .background(bubbleBrush)
                .shadow(6.dp, RoundedCornerShape(18.dp))
                .clickable(enabled = !isUserMessage) { onBubbleClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 300.dp)
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

@Composable
fun ModernMicButton(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val micColor by animateColorAsState(
        targetValue = if (isListening) Color(0xFF00E676) else Color(0xFF2196F3),
        animationSpec = tween(500)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isListening) 110.dp else 90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            micColor.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
                .blur(20.dp)
        )
        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = micColor),
            modifier = Modifier.size(if (isListening) 100.dp else 85.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
        ) {
            Text(
                text =  "🎙️",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
