package com.major_project.multilang_ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun SettingsScreen(navController: NavHostController,modifier: Modifier = Modifier) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Text("Preferred Language", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        AppLanguage.values().forEach { lang ->
            Button(
                onClick = {
                    UserPreferences.setLanguage(navController.context, lang.code)
                    navController.navigate("home") {
                        popUpTo("settings") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(lang.displayName)
            }
        }
    }
}
