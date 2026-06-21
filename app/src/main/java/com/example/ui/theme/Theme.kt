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
    primary = SkyPrimaryDark,
    secondary = SkySecondaryDark,
    tertiary = SkyTertiaryDark,
    background = SkyBackgroundDark,
    surface = SkySurfaceDark,
    onPrimary = SkyOnPrimaryDark,
    onSecondary = SkyOnPrimaryDark,
    onBackground = SkyOnBackgroundDark,
    onSurface = SkyOnSurfaceDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SkyPrimary,
    secondary = SkySecondary,
    tertiary = SkyTertiary,
    background = SkyBackground,
    surface = SkySurface,
    onPrimary = SkyOnPrimary,
    onSecondary = SkyOnSecondary,
    onBackground = SkyOnBackground,
    onSurface = SkyOnBackground
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to enforce our premium sky-blue design!
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
