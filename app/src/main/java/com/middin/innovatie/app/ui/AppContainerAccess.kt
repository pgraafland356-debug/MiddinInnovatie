package com.middin.innovatie.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.middin.innovatie.app.AppContainer
import com.middin.innovatie.app.MiddinApplication

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as MiddinApplication).container
    }
}
