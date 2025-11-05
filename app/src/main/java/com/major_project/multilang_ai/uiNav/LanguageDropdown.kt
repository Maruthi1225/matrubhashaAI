package com.major_project.multilang_ai.uiNav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.major_project.multilang_ai.ui.theme.AppThemeColors
import com.major_project.multilang_ai.voice.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val currentLang = AppLanguage.values().firstOrNull { it.code == selectedLanguage }?.displayName
        ?: "Select Language"

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = AppThemeColors.textColorPrimary()
            )
        ) {
            Text(
                text = currentLang,
                color = AppThemeColors.textColorPrimary(),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppThemeColors.cardColor())
        ) {
            AppLanguage.values().forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = lang.displayName,
                            color = AppThemeColors.textColorPrimary()
                        )
                    },
                    onClick = {
                        expanded = false
                        onLanguageChange(lang.code)
                    }
                )
            }
        }
    }
}
