package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhite,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = TealAccent,
    onSecondary = PureWhite,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = EmeraldDeep,
    tertiary = AmberGold,
    onTertiary = PureWhite,
    tertiaryContainer = SoftGold,
    onTertiaryContainer = AmberGold,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    outlineVariant = Slate100,
    error = ErrorRed,
    onError = PureWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Slate900,
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = EmeraldContainer,
    secondary = TealAccent,
    onSecondary = PureWhite,
    secondaryContainer = Slate800,
    onSecondaryContainer = PureWhite,
    tertiary = AmberGold,
    onTertiary = Slate900,
    tertiaryContainer = SoftGold,
    onTertiaryContainer = AmberGold,
    background = Color(0xFF0B1319),
    onBackground = Slate50,
    surface = Color(0xFF131F27),
    onSurface = Slate50,
    surfaceVariant = Color(0xFF1C2D37),
    onSurfaceVariant = Slate200,
    outline = Color(0xFF2E4452),
    outlineVariant = Color(0xFF1F313D),
    error = Color(0xFFF87171),
    onError = Slate900
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent emerald banking identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
