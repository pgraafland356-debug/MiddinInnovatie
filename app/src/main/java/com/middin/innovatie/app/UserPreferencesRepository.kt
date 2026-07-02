package com.middin.innovatie.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.middin.innovatie.app.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(
    private val context: Context,
    private val defaultApiBaseUrl: String,
) {
    private object Keys {
        val loggedIn = booleanPreferencesKey("logged_in")
        val username = stringPreferencesKey("username")
        val authToken = stringPreferencesKey("auth_token")
        val localeTag = stringPreferencesKey("locale_tag")
        val apiBaseUrlOverride = stringPreferencesKey("api_base_url_override")
        val updateFeedUrlOverride = stringPreferencesKey("update_feed_url_override")
        val updateNoticeDismissedCode = intPreferencesKey("update_notice_dismissed_code")
        val themeMode = stringPreferencesKey("theme_preference")
        val geminiApiKey = stringPreferencesKey("gemini_api_key")
        /** Debug only: prefer offline/local login. Ignored in release builds. */
        val useLocalSignIn = booleanPreferencesKey("use_local_sign_in")
    }

    val session: Flow<Boolean> = context.dataStore.data.map { it[Keys.loggedIn] == true }

    val username: Flow<String?> = context.dataStore.data.map { it[Keys.username] }
    val authToken: Flow<String?> = context.dataStore.data.map { it[Keys.authToken] }
    val localeTag: Flow<String> = context.dataStore.data.map { it[Keys.localeTag] ?: "en" }

    val themePreference: Flow<ThemePreference> = context.dataStore.data.map { prefs ->
        ThemePreference.fromStorage(prefs[Keys.themeMode])
    }

    val geminiApiKey: Flow<String?> = context.dataStore.data.map { prefs ->
        if (!canConfigureEndpoints(prefs[Keys.username])) null else prefs[Keys.geminiApiKey]
    }

    /**
     * Debug only: when true, login skips the API. Release builds always see false.
     * Default when unset follows [BuildConfig.USE_LOCAL_SIGN_IN] (typically true in debug).
     */
    val useLocalSignIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        effectiveLocalSignIn(prefs)
    }

    private fun effectiveLocalSignIn(prefs: Preferences): Boolean {
        if (!BuildConfig.DEBUG) return false
        return prefs[Keys.useLocalSignIn] ?: BuildConfig.USE_LOCAL_SIGN_IN
    }

    suspend fun isLocalSignInEnabled(): Boolean {
        val prefs = context.dataStore.data.first()
        return effectiveLocalSignIn(prefs)
    }

    suspend fun setUseLocalSignIn(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.useLocalSignIn] = enabled
        }
    }

    /** Raw override from storage; null or blank means “use build default”. */
    val apiBaseUrlOverride: Flow<String?> = context.dataStore.data.map { it[Keys.apiBaseUrlOverride] }

    /** Base URL used for API calls (override or [defaultApiBaseUrl]). */
    val effectiveApiBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        if (!canConfigureEndpoints(prefs[Keys.username])) {
            defaultApiBaseUrl.trimEnd('/')
        } else {
            resolveWithDefault(prefs[Keys.apiBaseUrlOverride])
        }
    }

    suspend fun resolvedApiBaseUrl(): String {
        val prefs = context.dataStore.data.first()
        return if (!canConfigureEndpoints(prefs[Keys.username])) {
            defaultApiBaseUrl.trimEnd('/')
        } else {
            resolveWithDefault(prefs[Keys.apiBaseUrlOverride])
        }
    }

    /** Raw update feed override from storage; null or blank means build default. */
    val updateFeedUrlOverride: Flow<String?> = context.dataStore.data.map { it[Keys.updateFeedUrlOverride] }

    val effectiveUpdateFeedUrl: Flow<String> = context.dataStore.data.map { prefs ->
        if (!canConfigureEndpoints(prefs[Keys.username])) {
            BuildConfig.UPDATE_FEED_URL.trim()
        } else {
            resolveUpdateFeedWithDefault(prefs[Keys.updateFeedUrlOverride])
        }
    }

    suspend fun resolvedUpdateFeedUrl(): String {
        val prefs = context.dataStore.data.first()
        return if (!canConfigureEndpoints(prefs[Keys.username])) {
            BuildConfig.UPDATE_FEED_URL.trim()
        } else {
            resolveUpdateFeedWithDefault(prefs[Keys.updateFeedUrlOverride])
        }
    }

    private fun resolveUpdateFeedWithDefault(override: String?): String {
        val o = override?.trim().orEmpty()
        return if (o.isNotEmpty()) o else BuildConfig.UPDATE_FEED_URL.trim()
    }

    private fun resolveWithDefault(override: String?): String {
        val o = override?.trim().orEmpty()
        return if (o.isNotEmpty()) o.trimEnd('/') else defaultApiBaseUrl.trimEnd('/')
    }

    suspend fun setApiBaseUrlOverride(url: String?) {
        context.dataStore.edit { prefs ->
            val trimmed = url?.trim().orEmpty()
            if (trimmed.isEmpty()) {
                prefs.remove(Keys.apiBaseUrlOverride)
            } else {
                prefs[Keys.apiBaseUrlOverride] = trimmed.trimEnd('/')
            }
        }
    }

    suspend fun setUpdateFeedUrlOverride(url: String?) {
        context.dataStore.edit { prefs ->
            val trimmed = url?.trim().orEmpty()
            if (trimmed.isEmpty()) {
                prefs.remove(Keys.updateFeedUrlOverride)
            } else {
                prefs[Keys.updateFeedUrlOverride] = trimmed
            }
        }
    }

    suspend fun getUpdateNoticeDismissedCode(): Int {
        return context.dataStore.data.first()[Keys.updateNoticeDismissedCode] ?: 0
    }

    suspend fun setUpdateNoticeDismissedCode(versionCode: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.updateNoticeDismissedCode] = versionCode
        }
    }

    suspend fun setAuthenticatedSession(username: String, token: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.loggedIn] = true
            prefs[Keys.username] = username
            prefs[Keys.authToken] = token
        }
    }

    suspend fun setSession(loggedIn: Boolean, username: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.loggedIn] = loggedIn
            if (username != null) prefs[Keys.username] = username
            if (!loggedIn) {
                prefs.remove(Keys.username)
                prefs.remove(Keys.authToken)
            }
        }
    }

    suspend fun setLocaleTag(tag: String) {
        context.dataStore.edit { it[Keys.localeTag] = tag }
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        context.dataStore.edit { it[Keys.themeMode] = preference.name.lowercase() }
    }

    suspend fun setGeminiApiKey(key: String?) {
        context.dataStore.edit { prefs ->
            val t = key?.trim().orEmpty()
            if (t.isEmpty()) prefs.remove(Keys.geminiApiKey) else prefs[Keys.geminiApiKey] = t
        }
    }

    companion object {
        /** Username that may configure Gemini, API base URL, and update feed (Settings + runtime). */
        const val ENDPOINT_SETTINGS_USERNAME = "pieter-bas"

        fun canConfigureEndpoints(username: String?): Boolean =
            username?.trim().equals(ENDPOINT_SETTINGS_USERNAME, ignoreCase = true) == true
    }
}
