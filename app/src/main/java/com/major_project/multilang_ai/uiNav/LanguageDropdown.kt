package com.major_project.multilang_ai.uiNav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.major_project.multilang_ai.ui.theme.AppThemeColors
import com.major_project.multilang_ai.voice.AppLanguage

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
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = AppThemeColors.cardColor(),
            tonalElevation = 2.dp,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentLang,
                    color = AppThemeColors.textColorPrimary(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppThemeColors.textColorPrimary().copy(alpha = 0.5f)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AppLanguage.values().forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (lang.code == selectedLanguage) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onLanguageChange(lang.code)
                    },
                    leadingIcon = {
                        if (lang.code == selectedLanguage) {
                            Icon(Icons.Default.Language, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        } else {
                            Spacer(modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }
        }
    }
}
