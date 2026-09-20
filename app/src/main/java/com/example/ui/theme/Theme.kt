package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = GalleryPrimary,
  onPrimary = Color.White,
  primaryContainer = GalleryPrimaryContainer,
  onPrimaryContainer = GalleryOnPrimaryContainer,
  secondary = GallerySecondary,
  onSecondary = Color.White,
  secondaryContainer = GallerySecondaryContainer,
  onSecondaryContainer = Color.White,
  tertiary = GalleryTertiaryDark,
  onTertiary = Color.White,
  background = GalleryDarkNeutral,
  onBackground = GalleryDarkTextPrimary,
  surface = GalleryDarkSurface,
  onSurface = GalleryDarkTextPrimary,
  surfaceVariant = GalleryDarkSurfaceVariant,
  onSurfaceVariant = GalleryDarkTextSecondary,
  outline = GalleryDarkOutline,
  surfaceContainer = GalleryDarkCard
)

private val LightColorScheme = lightColorScheme(
  primary = GalleryPrimary,
  onPrimary = Color.White,
  primaryContainer = GalleryOnPrimaryContainer,
  onPrimaryContainer = GalleryPrimaryContainer,
  secondary = GallerySecondary,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFDBEAFE),
  onSecondaryContainer = Color(0xFF1E3A8A),
  tertiary = GalleryTertiary,
  onTertiary = Color.White,
  background = GalleryLightNeutral,
  onBackground = GalleryLightTextPrimary,
  surface = GalleryLightSurface,
  onSurface = GalleryLightTextPrimary,
  surfaceVariant = GalleryLightSurfaceVariant,
  onSurfaceVariant = GalleryLightTextSecondary,
  outline = GalleryLightOutline,
  surfaceContainer = GalleryLightCard
)

@Composable
fun ManukxArtTheme(
  darkTheme: Boolean = true, // Default to gallery obsidian aesthetic per design mockup
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

