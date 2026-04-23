package com.middin.innovatie.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.middin.innovatie.app.ui.MiddinApp
import com.middin.innovatie.app.ui.theme.MiddinTheme
import com.middin.innovatie.app.ui.theme.ThemePreference
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_SCREEN_OFF) return
            val app = context?.applicationContext as? MiddinApplication ?: return
            app.clearSessionAsync()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startService(Intent(this, LogoutOnTaskRemovedService::class.java))
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
                val activity = LocalContext.current as Activity
                val view = LocalView.current
                SideEffect {
                    val window = activity.window
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
                // Edge-to-edge: the window is transparent; paint the full screen so login / welcome match the theme.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MiddinApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(screenOffReceiver, filter)
        }
    }

    override fun onStop() {
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (_: IllegalArgumentException) {
            // Already unregistered
        }
        super.onStop()
    }

    /**
     * When the task is removed (back out, swipe from recents) the session is cleared so the next
     * launch shows the login screen. Configuration changes call [onDestroy] with [isFinishing] false,
     * so the user stays signed in while the activity is recreated.
     */
    override fun onDestroy() {
        if (isFinishing) {
            val app = applicationContext as MiddinApplication
            runBlocking {
                app.container.userPreferences.setSession(loggedIn = false)
            }
        }
        super.onDestroy()
    }
}
