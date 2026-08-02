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

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalPharmaBlue,
    onPrimary = CrispWhite,
    primaryContainer = Color(0xFFE0EDFF),
    onPrimaryContainer = PharmaBlueDark,
    secondary = EmeraldGreen,
    onSecondary = CrispWhite,
    secondaryContainer = EmeraldGreenLight,
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = ExpiryAmber,
    onTertiary = CrispWhite,
    background = SoftPaperGray,
    onBackground = TextPrimary,
    surface = CrispWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = BorderGray
  )

private val DarkColorScheme = LightColorScheme // Force consistent professional B2B medical theme

@Composable
fun PharmaBazaarTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent B2B branding
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}
