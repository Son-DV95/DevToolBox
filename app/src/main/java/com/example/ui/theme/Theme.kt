package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CleanDarkPrimary,
    onPrimary = CleanDarkOnPrimary,
    primaryContainer = CleanDarkPrimaryContainer,
    onPrimaryContainer = CleanDarkOnPrimaryContainer,
    secondary = CleanDarkSecondary,
    secondaryContainer = CleanDarkSecondaryContainer,
    onSecondaryContainer = CleanDarkOnSecondaryContainer,
    background = CleanDarkBackground,
    surface = CleanDarkSurface,
    onBackground = CleanDarkOnBackground,
    onSurface = CleanDarkOnSurface,
    surfaceVariant = CleanDarkSurfaceVariant,
    onSurfaceVariant = CleanDarkOnSurfaceVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CleanPrimary,
    onPrimary = CleanOnPrimary,
    primaryContainer = CleanPrimaryContainer,
    onPrimaryContainer = CleanOnPrimaryContainer,
    secondary = CleanSecondary,
    secondaryContainer = CleanSecondaryContainer,
    onSecondaryContainer = CleanOnSecondaryContainer,
    tertiary = CleanTertiary,
    background = CleanBackground,
    surface = CleanSurface,
    onBackground = CleanOnBackground,
    onSurface = CleanOnSurface,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = CleanOnSurfaceVariant,
    outline = CleanOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
