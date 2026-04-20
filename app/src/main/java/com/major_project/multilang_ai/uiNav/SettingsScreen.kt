package com.major_project.multilang_ai.uiNav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.major_project.multilang_ai.voice.AppLanguage
import com.major_project.multilang_ai.uiNav.UserPreferences
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
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = AppThemeColors.textColorPrimary()) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Back", color = AppThemeColors.textColorPrimary())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Profile Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = AppThemeColors.cardColor(),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        val displayName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "User"
                        Text(
                            text = displayName,
                            color = AppThemeColors.textColorPrimary(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "Not logged in",
                            color = AppThemeColors.textColorPrimary().copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Text(
                "Personalization",
                color = AppThemeColors.textColorPrimary().copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            AuroraSettingCard(
                title = "Preferred Language",
                value = AppLanguage.values()
                    .firstOrNull { it.code == UserPreferences.getLanguage(navController.context) }?.displayName
                    ?: "Telugu",
                onClick = { showLangSheet = true }
            )

            AuroraToggle(
                title = "Dark Mode",
                checked = darkTheme,
                onToggle = {
                    darkTheme = it
                    UserPreferences.setDarkTheme(navController.context, it)
                    onThemeChange(it)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Logout", fontWeight = FontWeight.Bold)
            }

            Text(
                "Matrubhasha AI v1.0",
                color = AppThemeColors.textColorPrimary().copy(alpha = 0.4f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Modal Bottom Sheet for language selection
        if (showLangSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLangSheet = false },
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                containerColor = AppThemeColors.cardColor(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .heightIn(min = 220.dp, max = 420.dp)
                ) {
                    Text(
                        text = "Choose Language",
                        color = AppThemeColors.textColorPrimary(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    UserPreferences.setLanguage(navController.context, lang.code)
                                    voice.init(lang.code)
                                    showLangSheet = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.displayName,
                                color = AppThemeColors.textColorPrimary(),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showLangSheet = false }) {
                            Text("Close", color = Color(0xFF00E676), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuroraSettingCard(title: String, value: String?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 5.dp,
        color = AppThemeColors.cardColor()
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF00D4FF), Color(0xFF3A7BD5)).map { it.copy(alpha = 0.12f) }
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = AppThemeColors.textColorPrimary(), fontSize = 16.sp)
            if (value != null)
                Text(value, color = AppThemeColors.textColorPrimary().copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

@Composable
fun AuroraToggle(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val glowColor by animateColorAsState(
        if (checked) Color(0xFF00E676) else Color(0xFF607D8B),
        animationSpec = tween(600)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 5.dp,
        color = AppThemeColors.cardColor()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = AppThemeColors.textColorPrimary(), fontSize = 16.sp)
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = glowColor,
                    checkedTrackColor = glowColor.copy(alpha = 0.4f),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}
