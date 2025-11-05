// File: com/major_project/multilang_ai/ui/theme/AppTheme.kt
package com.major_project.multilang_ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// CompositionLocal that tells the app whether dark mode is enabled (app-level)
val LocalAppDarkTheme = staticCompositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean? = null, // null = follow system, true/false = override
    content: @Composable () -> Unit
) {
    val useDark = darkTheme ?: isSystemInDarkTheme()

    val darkColorScheme = darkColorScheme(
        primary = Color(0xFF00D4FF),
        onPrimary = Color.Black,
        background = Color(0xFF0A0F1F),
        onBackground = Color.White,
        surface = Color(0xFF121826),
        onSurface = Color.White,
        secondary = Color(0xFF9D50BB)
    )

    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF2196F3),
        onPrimary = Color.White,
        background = Color(0xFFF3F7FF),
        onBackground = Color.Black,
        surface = Color(0xFFFFFFFF),
        onSurface = Color.Black,
        secondary = Color(0xFF8E24AA)
    )

    val colors = if (useDark) darkColorScheme else lightColorScheme

    CompositionLocalProvider(LocalAppDarkTheme provides useDark) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography(), // keep your typography if you have one
            content = content
        )
    }
}


object AppThemeColors {

    @Composable
    fun isDark(): Boolean = LocalAppDarkTheme.current

    @Composable
    fun backgroundGradient(): Brush {
        return if (isDark()) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0F1F),
                    Color(0xFF1B2B50),
                    Color(0xFF3C3B6E)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF3F7FF),
                    Color(0xFFDCEBFF),
                    Color(0xFFBBD7FF)
                )
            )
        }
    }

    @Composable
    fun cardColor(): Color {
        return if (isDark()) Color.White.copy(alpha = 0.06f) else Color(0xFFF6F8FA)
    }

    @Composable
    fun textColorPrimary(): Color {
        return if (isDark()) Color.White else Color.Black
    }

    @Composable
    fun accentGradient(isUser: Boolean): List<Color> {
        return if (isDark()) {
            if (isUser) listOf(Color(0xFF00D4FF), Color(0xFF0A82FF))
            else listOf(Color(0xFF9D50BB), Color(0xFF6E48AA))
        } else {
            if (isUser) listOf(Color(0xFF2196F3), Color(0xFF42A5F5))
            else listOf(Color(0xFF8E24AA), Color(0xFFBA68C8))
        }
    }
}
