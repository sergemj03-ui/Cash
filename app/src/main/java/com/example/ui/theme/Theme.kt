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

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreenLight,
    onPrimary = Color.Black,
    primaryContainer = EmeraldGreenDark,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = GoldAccentDark,
    onSecondaryContainer = Color.White,
    tertiary = TechBlueLight,
    onTertiary = Color.White,
    background = DarkNavyBackground,
    onBackground = TextPrimaryLight,
    surface = DarkNavySurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = EmeraldGreenContainer,
    onPrimaryContainer = EmeraldGreenDark,
    secondary = GoldAccentDark,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = Color(0xFF5D4037),
    tertiary = TechBlue,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryDark,
    surface = LightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep CashLink brand colors consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
