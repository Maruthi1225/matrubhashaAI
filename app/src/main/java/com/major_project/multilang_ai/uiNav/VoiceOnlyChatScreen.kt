package com.major_project.multilang_ai.uiNav

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
import com.major_project.multilang_ai.voice.VoiceManager
import com.major_project.multilang_ai.ui.theme.AppThemeColors
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

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

    val backgroundGradient = AppThemeColors.backgroundGradient()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient),
        containerColor = Color.Transparent,
        bottomBar = {
            GlowingMicButton(
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
                .background(backgroundGradient)
                .padding(padding)
                .clickable { voice.pauseTTS() }
        ) {
            if (messages.isEmpty()) {
                EmptyChatPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎧 Speak to Matrubhasha AI",
                color = AppThemeColors.textColorPrimary(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Your multilingual voice assistant is listening...",
                color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onBubbleClick: () -> Unit) {
    val isUserMessage = message.isUser
    val bubbleColors = AppThemeColors.accentGradient(isUserMessage)
    val alignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        contentAlignment = alignment
    ) {
        Box(modifier = Modifier.widthIn(max = 320.dp)) {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .blur(18.dp)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = bubbleColors.map { it.copy(alpha = 0.25f) }
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clickable(enabled = !isUserMessage) { onBubbleClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = AppThemeColors.textColorPrimary(),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GlowingMicButton(isListening: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition()
    val glowRadius by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isListening) 50f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val micColor by animateColorAsState(
        targetValue = if (isListening) Color(0xFF00E676) else Color(0xFF3F51B5),
        animationSpec = tween(400)
    )

    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(micColor.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
                .blur(glowRadius.dp)
        )

        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = micColor),
            modifier = Modifier.size(95.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
        ) {
            Text(
                text = if (isListening) "🟢" else "🎙️",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
