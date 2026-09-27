package com.example.fitnessclub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ClubDarkColorScheme = darkColorScheme(
    primary = ClubPrimary,
    onPrimary = ClubOnPrimary,
    background = ClubBackground,
    onBackground = ClubText,
    surface = ClubSurface,
    onSurface = ClubText,
    surfaceVariant = ClubSurfaceVariant,
    onSurfaceVariant = ClubTextSecondary,
    error = ClubError,
    onError = ClubBackground
)

@Composable
fun FitnessClubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ClubDarkColorScheme,
        typography = ClubTypography,
        content = content
    )
}