package com.major_project.multilang_ai.uiNav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.major_project.multilang_ai.R
import com.major_project.multilang_ai.sarvam.ChatRepository
import com.major_project.multilang_ai.sarvam.ChatSession
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val chatRepo = remember { ChatRepository() }

    var chatHistory by remember { mutableStateOf(emptyList<ChatSession>()) }
    var selectedLanguage by remember { mutableStateOf(UserPreferences.getLanguage(navController.context)) }
    val backgroundGradient = AppThemeColors.backgroundGradient()

    // Fetch history whenever the drawer is opened
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            val history = chatRepo.getChatHistory()
            chatHistory = history
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("Chat History", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()

                if (chatHistory.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No history found", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chatHistory) { session ->
                        NavigationDrawerItem(
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = session.title.ifEmpty { "Untitled Chat" },
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (session.isPinned) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Pinned",
                                            tint = Color.Cyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    title = { Text("Matrubhasha AI", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = {
                            localAI.clearChat()
                            scope.launch { drawerState.close() }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "New Chat")
                        }

                        LanguageDropdown(
                            selectedLanguage = selectedLanguage,
                            onLanguageChange = {
                                selectedLanguage = it
                                UserPreferences.setLanguage(navController.context, it)
                            }
                        )

                        IconButton(onClick = { navController.navigate("settings") }) {
                            Image(painter = painterResource(id = R.drawable.settings), contentDescription = "Settings", modifier = Modifier.size(24.dp))
                        }
                    }
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.background(backgroundGradient)
        ) { paddingValues ->
            VoiceOnlyChatScreen(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                onVoiceInput = { onRecognized ->
                    scope.launch {
                        voice.listen(
                            onResult = { text, langCode ->
                                scope.launch {
                                    voice.pauseTTS()
                                    val reply = localAI.getResponse(text, langCode)
                                    onRecognized(text)
                                    voice.speakInstant(reply, langCode)
                                    chatRepo.saveChat(ChatSession(title = text, messages = listOf()))
                                }
                            },
                            onError = { println("Voice Error: $it") }
                        )
                    }
                },
                onSendMessage = { userText, onResponse ->
                    scope.launch {
                        voice.pauseTTS()
                        val reply = localAI.getResponse(userText, selectedLanguage)
                        onResponse(reply)
                        voice.speakInstant(reply, selectedLanguage)
                        chatRepo.saveChat(ChatSession(title = userText, messages = listOf()))
                    }
                },
                voice = voice,
                selectedLanguage = selectedLanguage
            )
        }
    }
}