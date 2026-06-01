package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MusicyColorScheme =
  lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = AccentPurpleLight,
    onPrimaryContainer = OnBackgroundDark,
    secondary = SecondaryLavender,
    onSecondary = TextGrey,
    tertiary = SurfaceVariantPurple,
    background = BackgroundLight,
    onBackground = OnBackgroundDark,
    surface = Color.White,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantPurple,
    onSurfaceVariant = TextGrey,
    outline = OutlineGrey
  )

@Composable
fun MusicyTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = MusicyColorScheme,
    typography = Typography,
    content = content
  )
}
