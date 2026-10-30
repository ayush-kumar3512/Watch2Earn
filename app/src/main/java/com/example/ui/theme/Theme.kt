package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = PrimaryVioletDark,
    onPrimaryContainer = Color.White,
    secondary = AccentGold,
    onSecondary = Color.Black,
    secondaryContainer = AccentGoldDark,
    onSecondaryContainer = Color.White,
    background = BgCosmicDark,
    onBackground = TextPrimary,
    surface = GlassCardBg,
    onSurface = TextPrimary,
    surfaceVariant = Color(0x28302058),
    onSurfaceVariant = TextSecondary,
    outline = GlassCardBorder,
    error = StatusError,
    onError = Color.White
)

@Composable
fun Watch2EarnTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
