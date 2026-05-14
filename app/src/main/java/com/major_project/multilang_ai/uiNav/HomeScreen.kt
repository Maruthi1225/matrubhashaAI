package com.major_project.multilang_ai.uiNav

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.major_project.multilang_ai.R
import com.major_project.multilang_ai.sarvam.ChatRepository
import com.major_project.multilang_ai.sarvam.ChatSession
import com.major_project.multilang_ai.sarvam.LocalAIService
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
    val snackbarHostState = remember { SnackbarHostState() }

    var chatHistory by remember { mutableStateOf(emptyList<ChatSession>()) }
    var currentSession by remember { mutableStateOf<ChatSession?>(null) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    
    var selectedLanguage by remember { mutableStateOf(UserPreferences.getLanguage(navController.context)) }
    var isListening by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    
//    val backgroundGradient = AppThemeColors.backgroundGradient()

    // State for renaming/deleting
    var sessionToRename by remember { mutableStateOf<ChatSession?>(null) }
    var renameText by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }

    val isDark = AppThemeColors.isDark()
    val logoResId = if (isDark) com.major_project.multilang_ai.R.drawable.dark else R.drawable.light
    val backgroundGradient = AppThemeColors.backgroundGradient()

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

    // Modern Dialogs
    if (sessionToRename != null) {
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("Rename Chat", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Session Name") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val target = sessionToRename
                    if (target != null && renameText.isNotBlank()) {
                        scope.launch {
                            chatRepo.updateChatTitle(target.id, renameText)
                            chatHistory = chatRepo.getChatHistory()
                            if (currentSession?.id == target.id) {
                                currentSession = currentSession?.copy(title = renameText)
                            }
                            sessionToRename = null
                        }
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Chat?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val target = sessionToDelete
                        if (target != null) {
                            scope.launch {
                                chatRepo.deleteChat(target.id)
                                chatHistory = chatRepo.getChatHistory()
                                if (currentSession?.id == target.id) startNewChat()
                                sessionToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerTonalElevation = 0.dp,
                modifier = Modifier.width(320.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Row(){
                            Image(
                                painter = painterResource(id = logoResId),
                                contentDescription = "Matrubhasha AI Logo",
                                modifier = Modifier.size(60.dp).align(Alignment.CenterVertically),
                                contentScale = ContentScale.Fit
                            )

                            Column{
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "MATRUBHASHA AI",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                )
                                Text(
                                    "The Indian Multilingual Voice Assistant",
                                    fontSize = 12.sp,
//                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Chat History",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        NavigationDrawerItem(
                            label = { Text("New Chat", fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = {
                                startNewChat()
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(Icons.Default.Add, null) },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    items(chatHistory) { session ->
                        var showMenu by remember { mutableStateOf(false) }

                        NavigationDrawerItem(
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = session.title.ifEmpty { "Untitled Chat" },
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (session.isPinned) {
                                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(14.dp), tint = Color(0xFF00E676))
                                    }
                                }
                            },
                            selected = session.id == currentSession?.id,
                            onClick = {
                                loadChat(session)
                                scope.launch { drawerState.close() }
                            },
                            shape = RoundedCornerShape(16.dp),
                            badge = {
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(if (session.isPinned) "Unpin" else "Pin") },
                                            onClick = {
                                                showMenu = false
                                                scope.launch {
                                                    chatRepo.togglePin(session.id, !session.isPinned)
                                                    chatHistory = chatRepo.getChatHistory()
                                                }
                                            },
                                            leadingIcon = { Icon(Icons.Default.PushPin, null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Rename") },
                                            onClick = {
                                                showMenu = false
                                                renameText = session.title
                                                sessionToRename = session
                                            },
                                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showMenu = false
                                                sessionToDelete = session
                                            },
                                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Matrubhasha AI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                if (currentSession == null) "New Conversation" else currentSession!!.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "History")
                        }
                    },
                    actions = {
                        LanguageDropdown(
                            selectedLanguage = selectedLanguage,
                            onLanguageChange = {
                                selectedLanguage = it
                                UserPreferences.setLanguage(navController.context, it)
                                voice.init(it)
                            }
                        )
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.background(backgroundGradient)
        ) { paddingValues ->
            VoiceOnlyChatScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                chatMessages = messages,
                isListening = isListening,
                isThinking = isThinking,
                onToggleListening = {
                    if (isListening) {
                        voice.stopListening()
                        isListening = false
                    } else {
                        voice.listen(
                            onResult = { userText, _ ->
                                scope.launch {
                                    voice.pauseTTS()
                                    messages.add(ChatMessage(userText, true))
                                    isThinking = true
                                    
                                    val reply = localAI.getResponse(userText, selectedLanguage)
                                    isThinking = false
                                    messages.add(ChatMessage(reply, false))
                                    voice.speakInstant(reply, selectedLanguage)

                                    val updatedHistory = localAI.getChatHistory()
                                    val sessionToSave = currentSession?.copy(
                                        title = if (currentSession == null) userText else currentSession!!.title,
                                        messages = updatedHistory
                                    ) ?: ChatSession(title = userText, messages = updatedHistory)

                                    val savedId = chatRepo.saveChat(sessionToSave)
                                    if (currentSession == null && savedId != null) {
                                        currentSession = sessionToSave.copy(id = savedId)
                                    }
                                }
                            },
                            onError = { errorMsg ->
                                scope.launch {
                                    isThinking = false
                                    snackbarHostState.showSnackbar(errorMsg)
                                }
                            },
                            onListeningStateChanged = { listening ->
                                isListening = listening
                            }
                        )
                    }
                },
                onBubbleClick = { msg ->
                    if (!msg.isUser) voice.resumeTTS(msg.text, selectedLanguage)
                },
                voice = voice
            )
        }
    }
}
