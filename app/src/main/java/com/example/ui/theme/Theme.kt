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

private val FarmLightColorScheme = lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = FarmGreenContainer,
    onPrimaryContainer = FarmGreenOnContainer,
    secondary = FarmGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E8D7),
    onSecondaryContainer = Color(0xFF0F3811),
    tertiary = FarmAmber,
    onTertiary = Color.White,
    tertiaryContainer = FarmAmberContainer,
    onTertiaryContainer = FarmAmberOnContainer,
    error = FarmRed,
    onError = Color.White,
    errorContainer = FarmRedContainer,
    onErrorContainer = FarmRedOnContainer,
    background = FarmBackground,
    onBackground = FarmTextPrimary,
    surface = FarmSurface,
    onSurface = FarmTextPrimary,
    surfaceVariant = FarmSurfaceVariant,
    onSurfaceVariant = FarmTextSecondary,
    outline = FarmOutline
)

private val FarmDarkColorScheme = darkColorScheme(
    primary = FarmGreenLight,
    onPrimary = Color(0xFF00390E),
    primaryContainer = FarmGreenPrimary,
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF00390E),
    background = Color(0xFF111411),
    onBackground = Color(0xFFE2E3DF),
    surface = Color(0xFF1A1D1A),
    onSurface = Color(0xFFE2E3DF),
    surfaceVariant = Color(0xFF262C26),
    onSurfaceVariant = Color(0xFFC3C8C2),
    outline = Color(0xFF4B534B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent high-contrast farm branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FarmDarkColorScheme
        else -> FarmLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
