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
    primary = PrimaryPolish,
    secondary = SecondaryPolish,
    background = BackgroundPolish,
    surface = SurfacePolish,
    onPrimary = OnPrimaryPolish,
    onBackground = OnBackgroundPolish,
    onSurface = OnSurfacePolish
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryPolish,
    onPrimary = OnPrimaryPolish,
    primaryContainer = PrimaryContainerPolish,
    onPrimaryContainer = OnPrimaryContainerPolish,
    secondary = SecondaryPolish,
    onSecondary = OnSecondaryPolish,
    secondaryContainer = SecondaryContainerPolish,
    onSecondaryContainer = OnSecondaryContainerPolish,
    background = BackgroundPolish,
    onBackground = OnBackgroundPolish,
    surface = SurfacePolish,
    onSurface = OnSurfacePolish,
    surfaceVariant = SurfaceVariantPolish,
    onSurfaceVariant = OnSurfaceVariantPolish,
    outline = OutlinePolish,
    outlineVariant = OutlineVariantPolish,
    error = ErrorPolish,
    errorContainer = ErrorContainerPolish,
    onErrorContainer = OnErrorContainerPolish
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Override dynamic color to ensure Professional Polish theme is prominent
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> LightColorScheme // Maintain clean light background by default for consistency
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
