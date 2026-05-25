package com.example.pechka.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OnSurfaceText = Color(0xFF221A11)

private val ArtisanalColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimaryGreen,
    primaryContainer = PrimaryContainerGreen,
    onPrimaryContainer = OnPrimaryGreen,
    secondary = PrimaryContainerGreen,
    onSecondary = OnPrimaryGreen,
    secondaryContainer = SecondaryContainerGray,
    onSecondaryContainer = OnSecondaryContainerGray,
    background = BackgroundGray,
    onBackground = OnSurfaceText,
    surface = BackgroundGray,
    onSurface = OnSurfaceText,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = SecondaryGray,
    outline = OutlineGray,
    outlineVariant = OutlineVariant,
    error = ErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed,
)

@Composable
fun PechkaTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ArtisanalColorScheme,
        typography = Typography,
        content = content,
    )
}
