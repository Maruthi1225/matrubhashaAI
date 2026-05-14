package com.major_project.multilang_ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

val LocalAppDarkTheme = staticCompositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val useDark = darkTheme ?: isSystemInDarkTheme()

    val darkColorScheme = darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        background = BackgroundDark,
        onBackground = Color.White,
        surface = SurfaceDark,
        onSurface = Color.White,
        secondary = SecondaryDark
    )

    val lightColorScheme = lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        background = BackgroundLight,
        onBackground = Color.Black,
        surface = SurfaceLight,
        onSurface = Color.Black,
        secondary = SecondaryLight
    )

    val colors = if (useDark) darkColorScheme else lightColorScheme

    CompositionLocalProvider(LocalAppDarkTheme provides useDark) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
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
                    BackgroundDark,
                    DeepIndigo,
                    Color(0xFF3C3B6E)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    BackgroundLight,
                    Color(0xFFDCEBFF),
                    Color(0xFFBBD7FF)
                )
            )
        }
    }

    @Composable
    fun cardColor(): Color {
        return if (isDark()) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.7f)
    }

    @Composable
    fun textColorPrimary(): Color {
        return if (isDark()) Color.White else Color.Black
    }

    @Composable
    fun accentGradient(isUser: Boolean): List<Color> {
        return if (isDark()) {
            if (isUser) listOf(CyberBlue, Color(0xFF0A82FF))
            else listOf(NeonPurple, Color(0xFF6E48AA))
        } else {
            if (isUser) listOf(PrimaryLight, Color(0xFF42A5F5))
            else listOf(SecondaryLight, Color(0xFFBA68C8))
        }
    }
}
