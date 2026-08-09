package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E8DC),
    onPrimaryContainer = Color(0xFF032113),
    secondary = GreenSecondary,
    onSecondary = Color.White,
    background = MintBackground,
    onBackground = Color(0xFF191C1A),
    surface = MintSurface,
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFE0E5E1),
    onSurfaceVariant = Color(0xFF404943),
    outline = Color(0xFF707973)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color(0xFF00391C),
    primaryContainer = GreenPrimaryDark,
    onPrimaryContainer = Color(0xFFB7F3CD),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF003816),
    background = DarkBackground,
    onBackground = Color(0xFFE1E3DF),
    surface = DarkSurface,
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC0C9C2),
    outline = Color(0xFF8A938C)
)

@Composable
fun MoneyManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
