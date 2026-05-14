package com.major_project.multilang_ai.uiNav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.major_project.multilang_ai.voice.AppLanguage
import com.major_project.multilang_ai.voice.VoiceManager
import com.major_project.multilang_ai.ui.theme.AppThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    voice: VoiceManager,
    onThemeChange: (Boolean) -> Unit
) {
    var showLangSheet by remember { mutableStateOf(false) }
    var darkTheme by remember { mutableStateOf(UserPreferences.isDarkTheme(navController.context)) }
    val gradient = AppThemeColors.backgroundGradient()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = AppThemeColors.textColorPrimary()
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = AppThemeColors.textColorPrimary()
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(gradient)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // User Profile Section (Premium Look)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = AppThemeColors.cardColor(),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column {
                        val displayName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "User"
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppThemeColors.textColorPrimary()
                            )
                        )
                        Text(
                            text = currentUser?.email ?: "Anonymous User",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f)
                        )
                    }
                }
            }

            SectionHeader("Personalization")

            AuroraSettingCard(
                title = "App Language",
                subtitle = AppLanguage.values()
                    .firstOrNull { it.code == UserPreferences.getLanguage(navController.context) }?.displayName
                    ?: "Select Language",
                icon = Icons.Default.Translate,
                onClick = { showLangSheet = true }
            )

            AuroraToggle(
                title = "Dark Theme",
                subtitle = "Toggle dark/light mode",
                icon = if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                checked = darkTheme,
                onToggle = {
                    darkTheme = it
                    UserPreferences.setDarkTheme(navController.context, it)
                    onThemeChange(it)
                }
            )

            SectionHeader("Account")

            AuroraSettingCard(
                title = "Logout",
                subtitle = "Sign out of your account",
                icon = Icons.AutoMirrored.Filled.Logout,
                color = MaterialTheme.colorScheme.error,
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Matrubhasha AI v1.5",
                style = MaterialTheme.typography.labelMedium,
                color = AppThemeColors.textColorPrimary().copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showLangSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLangSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        "Choose Language",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = AppThemeColors.textColorPrimary()
                        ),
                        modifier = Modifier.padding(24.dp)
                    )

                    AppLanguage.values().forEach { lang ->
                        ListItem(
                            headlineContent = { 
                                Text(
                                    lang.displayName,
                                    color = AppThemeColors.textColorPrimary()
                                ) 
                            },
                            leadingContent = { 
                                RadioButton(
                                    selected = lang.code == UserPreferences.getLanguage(navController.context),
                                    onClick = null 
                                )
                            },
                            modifier = Modifier.clickable {
                                UserPreferences.setLanguage(navController.context, lang.code)
                                voice.init(lang.code)
                                showLangSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(start = 8.dp)
    )
}

@Composable
fun AuroraSettingCard(
    title: String, 
    subtitle: String, 
    icon: ImageVector, 
    color: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = AppThemeColors.cardColor(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (color == Color.Unspecified) AppThemeColors.textColorPrimary() else color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppThemeColors.textColorPrimary().copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun AuroraToggle(
    title: String, 
    subtitle: String, 
    icon: ImageVector,
    checked: Boolean, 
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppThemeColors.cardColor(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppThemeColors.textColorPrimary()
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
