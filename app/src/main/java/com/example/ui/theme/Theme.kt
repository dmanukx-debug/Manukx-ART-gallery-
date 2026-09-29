package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudyPrimary,
    onPrimary = Color.White,
    primaryContainer = StudyPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFC7D2FE),
    secondary = StudySecondary,
    onSecondary = Color.Black,
    secondaryContainer = StudySecondaryContainer,
    onSecondaryContainer = Color.White,
    tertiary = StudyAccentAmber,
    onTertiary = Color.Black,
    background = StudyDarkBg,
    onBackground = StudyDarkTextPrimary,
    surface = StudyDarkSurface,
    onSurface = StudyDarkTextPrimary,
    surfaceVariant = StudyDarkCard,
    onSurfaceVariant = StudyDarkTextSecondary,
    outline = StudyDarkOutline,
    surfaceContainer = StudyDarkCard,
    surfaceContainerHigh = StudyDarkCardElevated
)

private val LightColorScheme = lightColorScheme(
    primary = StudyPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = StudyPrimaryContainerLight,
    onPrimaryContainer = StudyPrimaryLight,
    secondary = StudySecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = StudyAccentAmber,
    onTertiary = Color.White,
    background = StudyLightBg,
    onBackground = StudyLightTextPrimary,
    surface = StudyLightSurface,
    onSurface = StudyLightTextPrimary,
    surfaceVariant = StudyLightCardElevated,
    onSurfaceVariant = StudyLightTextSecondary,
    outline = StudyLightOutline,
    surfaceContainer = StudyLightCard,
    surfaceContainerHigh = Color(0xFFE2E8F0)
)

@Composable
fun StudyPlannerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
