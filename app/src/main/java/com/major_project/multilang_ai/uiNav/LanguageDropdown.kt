package com.major_project.multilang_ai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Display name ↔ code map
    val languageMap = mapOf(
        "English" to "en-IN",
        "Hindi" to "hi-IN",
        "Telugu" to "te-IN",
        "Tamil" to "ta-IN",
        "Kannada" to "kn-IN"
    )

    // Reverse lookup for displaying current selection
    val currentDisplayName = languageMap.entries.find { it.value == selectedLanguage }?.key ?: "te-IN"

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(currentDisplayName)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languageMap.forEach { (name, code) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onLanguageChange(code) // ✅ store code like "te-IN"
                        expanded = false
                    }
                )
            }
        }
    }
}
