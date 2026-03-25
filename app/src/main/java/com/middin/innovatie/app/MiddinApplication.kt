package com.middin.innovatie.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.middin.innovatie.app.notifications.NotificationHelper
import com.middin.innovatie.app.data.local.ProductCatalogSeed
import com.middin.innovatie.app.ui.theme.ThemePreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MiddinApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
        runBlocking {
            val productDao = container.database.productDao()
            ProductCatalogSeed.seedIfEmpty(productDao)
            ProductCatalogSeed.fillEmptyDescriptions(productDao)
            val tag = container.userPreferences.localeTag.first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
            val theme = container.userPreferences.themePreference.first()
            AppCompatDelegate.setDefaultNightMode(
                when (theme) {
                    ThemePreference.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    ThemePreference.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    ThemePreference.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                },
            )
        }
    }
}
