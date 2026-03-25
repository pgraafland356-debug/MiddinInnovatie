package com.middin.innovatie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.middin.innovatie.app.ui.MiddinApp
import com.middin.innovatie.app.ui.theme.MiddinTheme
import com.middin.innovatie.app.ui.theme.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = applicationContext as MiddinApplication
        setContent {
            val themePref by app.container.userPreferences.themePreference.collectAsStateWithLifecycle(
                initialValue = ThemePreference.SYSTEM,
            )
            val darkTheme = when (themePref) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }
            MiddinTheme(darkTheme = darkTheme) {
                MiddinApp()
            }
        }
    }
}
