package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = GreenDeep,
    secondary = OrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = OrangeSoft,
    onSecondaryContainer = OrangeDeep,
    tertiary = GreenDeep,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = DarkInk,
    surface = CreamSurface,
    onSurface = DarkInk,
    surfaceVariant = CreamElevated,
    onSurfaceVariant = MutedInk,
    outline = SubtleOutline,
    outlineVariant = SubtleOutline
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1F4722),
    onPrimaryContainer = GreenMuted,
    secondary = OrangeAccent,
    onSecondary = Color(0xFF2B1707),
    secondaryContainer = Color(0xFF4D2B10),
    onSecondaryContainer = OrangeSoft,
    tertiary = GreenMuted,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = LightInk,
    surface = DarkSurface,
    onSurface = LightInk,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = LightMutedInk,
    outline = Color(0xFF3A483D),
    outlineVariant = Color(0xFF2F3B31)
)

val GreenBiteShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun GreenBiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GreenBiteTypography,
        shapes = GreenBiteShapes,
        content = content
    )
}
