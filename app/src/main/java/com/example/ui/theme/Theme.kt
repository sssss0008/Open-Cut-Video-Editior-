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
    primary = PrimaryPurpleLight,
    onPrimary = Color(0xFF1E1145),
    primaryContainer = Color(0xFF381E72),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = SecondaryTeal,
    onSecondary = Color(0xFF003830),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFF73F8E4),
    tertiary = AccentCyan,
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F58),
    onTertiaryContainer = Color(0xFF9EEFFE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = StudioDarkBg,
    onBackground = Color(0xFFE6E1E5),
    surface = StudioSurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = StudioSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = StudioOutlineDark,
    outlineVariant = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF006A5F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF73F8E4),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF006874),
    onTertiary = Color.White,
    background = Color(0xFFFBF8FD),
    surface = Color(0xFFFBF8FD),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

@Composable
fun OpenCutTheme(
    darkTheme: Boolean = true, // Default to dark studio theme for professional video editing
    dynamicColor: Boolean = false,
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
