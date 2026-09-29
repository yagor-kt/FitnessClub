package com.example.fitnessclub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ClubDarkColorScheme = darkColorScheme(
    primary = ClubPrimary,
    onPrimary = ClubOnPrimary,
    secondary = ClubSecondary,
    onSecondary = ClubText,
    tertiary = ClubWarning,
    onTertiary = ClubOnPrimary,
    background = ClubBackground,
    onBackground = ClubText,
    surface = ClubSurface,
    onSurface = ClubText,
    surfaceVariant = ClubSurfaceVariant,
    onSurfaceVariant = ClubTextSecondary,
    error = ClubError,
    onError = ClubOnPrimary,
    primaryContainer = ClubPrimary.copy(alpha = 0.18f),
    onPrimaryContainer = ClubPrimary,
    secondaryContainer = ClubSecondary.copy(alpha = 0.22f),
    onSecondaryContainer = ClubText,
    tertiaryContainer = ClubWarning.copy(alpha = 0.2f),
    onTertiaryContainer = ClubWarning
)

@Composable
fun FitnessClubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ClubDarkColorScheme,
        typography = ClubTypography,
        content = content
    )
}