package com.middin.innovatie.app.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.middin.innovatie.app.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Persists the locale choice and applies it app-wide (resource locale + activity recreate when needed). */
object AppLocaleHelper {
    suspend fun apply(userPreferences: UserPreferencesRepository, tag: String) {
        userPreferences.setLocaleTag(tag)
        withContext(Dispatchers.Main.immediate) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }
}
