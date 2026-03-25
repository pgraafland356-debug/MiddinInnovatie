package com.middin.innovatie.app.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.middin.innovatie.app.UserPreferencesRepository
import com.middin.innovatie.app.ui.theme.ThemePreference
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {
    fun setLocale(tag: String) {
        viewModelScope.launch {
            userPreferences.setLocaleTag(tag)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }

    fun setTheme(preference: ThemePreference) {
        viewModelScope.launch {
            userPreferences.setThemePreference(preference)
            AppCompatDelegate.setDefaultNightMode(
                when (preference) {
                    ThemePreference.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    ThemePreference.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    ThemePreference.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                },
            )
        }
    }

    fun saveApiServerUrl(url: String) {
        viewModelScope.launch {
            userPreferences.setApiBaseUrlOverride(url)
        }
    }

    fun saveGeminiApiKey(key: String) {
        viewModelScope.launch {
            userPreferences.setGeminiApiKey(key)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.setSession(loggedIn = false)
        }
    }

    fun setUseLocalSignIn(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setUseLocalSignIn(enabled)
        }
    }

    companion object {
        fun factory(prefs: UserPreferencesRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(prefs) as T
        }
    }
}
