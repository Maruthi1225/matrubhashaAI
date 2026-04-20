package com.major_project.multilang_ai.uiNav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.major_project.multilang_ai.R
import com.major_project.multilang_ai.sarvam.ChatRepository
import com.major_project.multilang_ai.sarvam.ChatSession
import com.major_project.multilang_ai.sarvam.LocalAIService
import com.major_project.multilang_ai.sarvam.SarvamMessage
import com.major_project.multilang_ai.ui.theme.AppThemeColors
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
    var currentSession by remember { mutableStateOf<ChatSession?>(null) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    
    var selectedLanguage by remember { mutableStateOf(UserPreferences.getLanguage(navController.context)) }
    val backgroundGradient = AppThemeColors.backgroundGradient()

    // Fetch history whenever the drawer is opened
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            chatHistory = chatRepo.getChatHistory()
        }
    }

    fun startNewChat() {
        currentSession = null
        messages.clear()
        localAI.clearChat()
    }

    fun loadChat(session: ChatSession) {
        currentSession = session
        messages.clear()
        messages.addAll(session.messages.map { ChatMessage(it.content, it.role == "user") })
        localAI.setChatHistory(session.messages)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AppThemeColors.cardColor(),
                drawerTonalElevation = 8.dp
            ) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chat History", style = MaterialTheme.typography.titleLarge, color = AppThemeColors.textColorPrimary())
                    IconButton(onClick = { 
                        startNewChat()
                        scope.launch { drawerState.close() }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider(color = AppThemeColors.textColorPrimary().copy(alpha = 0.1f))

                if (chatHistory.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No history found", color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f))
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
                                        modifier = Modifier.weight(1f),
                                        color = AppThemeColors.textColorPrimary()
                                    )
                                    if (session.isPinned) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Pinned",
                                            tint = Color(0xFF00E676),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            selected = session.id == currentSession?.id,
                            onClick = {
                                loadChat(session)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
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
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AppThemeColors.textColorPrimary())
                        }
                    },
                    title = { Text("Matrubhasha AI", color = AppThemeColors.textColorPrimary(), fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = {
                            startNewChat()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "New Chat", tint = AppThemeColors.textColorPrimary())
                        }

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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.background(backgroundGradient)
        ) { paddingValues ->
            VoiceOnlyChatScreen(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                chatMessages = messages,
                onVoiceInput = { onRecognized ->
                    voice.listen(
                        onResult = { text, _ ->
                            scope.launch {
                                voice.stopListening()
                                onRecognized(text)
                            }
                        },
                        onError = { println("Voice Error: $it") }
                    )
                },
                onSendMessage = { userText, onResponse ->
                    scope.launch {
                        voice.pauseTTS()
                        
                        // Add user message to the list
                        messages.add(ChatMessage(userText, true))
                        
                        val reply = localAI.getResponse(userText, selectedLanguage)
                        
                        // Add AI reply to the list
                        messages.add(ChatMessage(reply, false))

                        onResponse(reply)
                        voice.speakInstant(reply, selectedLanguage)

                        val updatedMessages = localAI.getChatHistory()
                        val sessionToSave = currentSession?.copy(
                            title = if (currentSession == null) userText else currentSession!!.title,
                            messages = updatedMessages
                        ) ?: ChatSession(title = userText, messages = updatedMessages)

                        val savedId = chatRepo.saveChat(sessionToSave)
                        if (currentSession == null && savedId != null) {
                            currentSession = sessionToSave.copy(id = savedId)
                        }
                    }
                },
                voice = voice,
                selectedLanguage = selectedLanguage
            )
        }
    }
}
