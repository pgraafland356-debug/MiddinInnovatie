package com.middin.innovatie.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MiddinLightColorScheme = lightColorScheme(
    primary = MiddinBrandColors.Primary,
    onPrimary = MiddinBrandColors.White,
    primaryContainer = MiddinBrandColors.PrimaryLight,
    onPrimaryContainer = MiddinBrandColors.Primary,

    secondary = MiddinBrandColors.Secondary,
    onSecondary = MiddinBrandColors.Primary,
    secondaryContainer = MiddinBrandColors.SecondaryDark,
    onSecondaryContainer = MiddinBrandColors.Primary,

    tertiary = MiddinBrandColors.Green,
    onTertiary = Color(0xFF00332C),
    tertiaryContainer = MiddinBrandColors.SecondaryLight,
    onTertiaryContainer = MiddinBrandColors.Primary,

    background = MiddinBrandColors.Background,
    onBackground = MiddinBrandColors.Text,
    surface = MiddinBrandColors.Background,
    onSurface = MiddinBrandColors.Text,
    surfaceVariant = MiddinBrandColors.BackgroundLight,
    onSurfaceVariant = MiddinBrandColors.TextLight,

    outline = MiddinBrandColors.Border,
    outlineVariant = MiddinBrandColors.BorderInput,

    error = Color(0xFFB3261E),
    onError = MiddinBrandColors.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val MiddinDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B9AFF),
    onPrimary = Color(0xFF000C4A),
    primaryContainer = Color(0xFF002080),
    onPrimaryContainer = Color(0xFFDDE1FF),

    secondary = MiddinBrandColors.SecondaryLight,
    onSecondary = Color(0xFF0D1A1C),
    secondaryContainer = Color(0xFF3D5A60),
    onSecondaryContainer = Color(0xFFE8F4F6),

    tertiary = MiddinBrandColors.Green,
    onTertiary = Color(0xFF00332C),
    tertiaryContainer = Color(0xFF1A4D45),
    onTertiaryContainer = Color(0xFFB8F5E8),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E8ED),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFE8E8ED),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),

    outline = Color(0xFF6E6E73),
    outlineVariant = Color(0xFF48484A),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

@Composable
fun MiddinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color overrides brand; keep false so tokens match design system.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }

        darkTheme -> MiddinDarkColorScheme
        else -> MiddinLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MiddinTypography,
        content = content,
    )
}
