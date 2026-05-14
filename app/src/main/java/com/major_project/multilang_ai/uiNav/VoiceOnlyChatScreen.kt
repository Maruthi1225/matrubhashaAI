package com.major_project.multilang_ai.uiNav

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.major_project.multilang_ai.voice.VoiceManager
import com.major_project.multilang_ai.ui.theme.AppThemeColors

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun VoiceOnlyChatScreen(
    modifier: Modifier = Modifier,
    chatMessages: List<ChatMessage>,
    isListening: Boolean,
    isThinking: Boolean,
    onToggleListening: () -> Unit,
    onBubbleClick: (ChatMessage) -> Unit,
    voice: VoiceManager
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = { voice.pauseTTS() })
    ) {
        if (chatMessages.isEmpty() && !isThinking) {
            EmptyChatPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 140.dp),
                reverseLayout = true
            ) {
                if (isThinking) {
                    item {
                        ThinkingIndicator()
                    }
                }
                
                itemsIndexed(chatMessages.reversed()) { _, msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        ChatBubble(
                            message = msg,
                            onBubbleClick = { onBubbleClick(msg) }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            GlowingMicButton(
                isListening = isListening,
                onClick = onToggleListening
            )
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .padding(start = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppThemeColors.cardColor())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotAlpha"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer(alpha = dotAlpha)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "Thinking...",
            style = MaterialTheme.typography.labelSmall,
            color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f)
        )
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "placeholder")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Surface(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer(alpha = alpha),
            shape = CircleShape,
            color = AppThemeColors.cardColor(),
            tonalElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🎙️", fontSize = 48.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Matrubhasha AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                brush = Brush.linearGradient(AppThemeColors.accentGradient(true))
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your multilingual companion is ready",
            color = AppThemeColors.textColorPrimary().copy(alpha = 0.7f),
            fontSize = 16.sp
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onBubbleClick: () -> Unit) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(if (isUser) 8.dp else 4.dp, shape)
                .clip(shape)
                .background(
                    if (isUser) {
                        Brush.linearGradient(AppThemeColors.accentGradient(true))
                    } else {
                        Brush.linearGradient(
                            listOf(
                                AppThemeColors.cardColor(),
                                AppThemeColors.cardColor().copy(alpha = 0.9f)
                            )
                        )
                    }
                )
                .clickable(enabled = !isUser) { onBubbleClick() }
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.Black else AppThemeColors.textColorPrimary(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontWeight = if (isUser) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun GlowingMicButton(isListening: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "mic_glow")
    
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isListening) 0.7f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(contentAlignment = Alignment.Center) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF00E676).copy(alpha = glowAlpha), Color.Transparent)
                        )
                    )
            )
        }

        FloatingActionButton(
            onClick = onClick,
            containerColor = if (isListening) Color(0xFF00E676) else MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .size(72.dp)
                .scale(if (isListening) 1.1f else 1f),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_btn_speak_now),
                contentDescription = "Speak",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
