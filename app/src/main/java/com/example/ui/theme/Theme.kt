package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OrbitalBlue,
    onPrimary = SlateText,
    secondary = GlowBlue,
    background = SpaceBlack,
    surface = SpaceDarkGray,
    onBackground = SlateText,
    onSurface = SlateText,
    error = AlertOrange
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
