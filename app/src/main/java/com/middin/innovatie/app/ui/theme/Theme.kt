package com.middin.innovatie.app.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = MiddinPrimary,
    secondary = MiddinSecondary,
    tertiary = MiddinSurfaceTint,
)

private val DarkColors = darkColorScheme(
    primary = MiddinPrimary,
    secondary = MiddinSecondary,
    tertiary = MiddinSurfaceTint,
)

@Composable
fun MiddinTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MiddinTypography,
        content = content,
    )
}
