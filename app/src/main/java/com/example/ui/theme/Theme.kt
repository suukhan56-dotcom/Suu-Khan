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
    primary = MtaaOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF38140B),
    onPrimaryContainer = Color(0xFFFFCCBC),
    secondary = MtaaCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = MtaaMagenta,
    onTertiary = Color.White,
    background = MtaaDarkBg,
    onBackground = MtaaTextPrimary,
    surface = MtaaDarkSurface,
    onSurface = MtaaTextPrimary,
    surfaceVariant = MtaaDarkSurfaceVariant,
    onSurfaceVariant = MtaaTextSecondary,
    outline = MtaaDivider
)

private val LightColorScheme = lightColorScheme(
    primary = MtaaOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCCBC),
    onPrimaryContainer = Color(0xFF38140B),
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    tertiary = MtaaMagenta,
    onTertiary = Color.White,
    background = Color(0xFFF9F9FB),
    onBackground = Color(0xFF16161E),
    surface = Color.White,
    onSurface = Color(0xFF16161E),
    surfaceVariant = Color(0xFFF0F0F5),
    onSurfaceVariant = Color(0xFF555566),
    outline = Color(0xFFE2E2EA)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to immersive dark mode for video-first experience
    dynamicColor: Boolean = false, // Keep MtaaTok brand colors consistent
    content: @Composable () -> Unit,
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
