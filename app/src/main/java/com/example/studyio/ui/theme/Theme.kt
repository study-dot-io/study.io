package com.example.studyio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFFFFFFF),
    
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF047857),
    onSecondaryContainer = Color(0xFFFFFFFF),
    
    tertiary = Color(0xFFF59E20),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD97706),
    onTertiaryContainer = Color(0xFFFFFFFF),
    
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFDC2626),
    onErrorContainer = Color(0xFFFFFFFF),
    
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF64748B),
    
    scrim = Color(0x80000000),
    surfaceTint = Color(0xFF6366F1),
    inverseSurface = Color(0xFFF1F5F9),
    inverseOnSurface = Color(0xFF0F172A),
    inversePrimary = Color(0xFF818CF8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    
    secondary = Color(0xFF059669),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    
    tertiary = Color(0xFFD97706),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFED7AA),
    onTertiaryContainer = Color(0xFF451A03),
    
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    
    scrim = Color(0x80000000),
    surfaceTint = Color(0xFF4F46E5),
    inverseSurface = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFF8FAFC),
    inversePrimary = Color(0xFF818CF8)
)

@Composable
fun StudyIOTheme(
    darkTheme: Boolean = true,
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 