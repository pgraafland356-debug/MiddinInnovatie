package com.middin.innovatie.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.middin.innovatie.app.locale.AppLocaleHelper
import com.middin.innovatie.app.notifications.NotificationHelper
import com.middin.innovatie.app.data.local.ProductCatalogSeed
import com.middin.innovatie.app.ui.theme.ThemePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MiddinApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Clears login session asynchronously (e.g. device screen off / lock). */
    fun clearSessionAsync() {
        if (!::container.isInitialized) return
        appScope.launch {
            container.userPreferences.setSession(loggedIn = false)
        }
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
        runBlocking {
            // Do not restore login from the previous run: user must sign in again after each app (process) start.
            container.userPreferences.setSession(loggedIn = false)
            val productDao = container.database.productDao()
            ProductCatalogSeed.seedIfEmpty(productDao)
            ProductCatalogSeed.fillEmptyDescriptions(productDao)
            ProductCatalogSeed.removeObsoleteProducts(productDao)
            ProductCatalogSeed.syncMissingCatalogEntries(productDao)
            val tag = container.userPreferences.localeTag.first()
            AppLocaleHelper.apply(container.userPreferences, tag)
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
