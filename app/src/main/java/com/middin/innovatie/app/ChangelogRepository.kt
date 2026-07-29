package com.middin.innovatie.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Loads release history from [releases/changelog.json] (bundled + optional remote feed).
 * Remote URL is derived from the update feed by replacing latest.json with changelog.json.
 */
class ChangelogRepository(
    private val context: Context,
    private val feedUrlProvider: suspend () -> String,
) {
    private val _items = MutableStateFlow(bundledItems())
    val items: StateFlow<List<ChangelogItem>> = _items.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val bundled = loadBundledEntries()
        val remote = fetchRemoteEntries(feedUrlProvider())
        val merged = ChangelogJson.merge(remote, bundled)
        _items.value = withDevBuildHint(merged)
    }

    private fun bundledItems(): List<ChangelogItem> =
        withDevBuildHint(ChangelogJson.toItems(loadBundledEntriesSync()))

    private fun loadBundledEntriesSync(): List<ChangelogJson.Entry> {
        return try {
            context.assets.open("changelog.json").bufferedReader().use { reader ->
                ChangelogJson.parse(reader.readText())
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun loadBundledEntries(): List<ChangelogJson.Entry> =
        withContext(Dispatchers.IO) { loadBundledEntriesSync() }

    private suspend fun fetchRemoteEntries(updateFeedUrl: String): List<ChangelogJson.Entry> {
        val changelogUrl = ChangelogJson.changelogUrlFromUpdateFeed(updateFeedUrl)
        if (changelogUrl.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val conn = (URL(changelogUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }
                try {
                    if (conn.responseCode !in 200..299) return@withContext emptyList()
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    ChangelogJson.parse(body)
                } finally {
                    conn.disconnect()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun withDevBuildHint(items: List<ChangelogItem>): List<ChangelogItem> {
        val newestCode = items.maxOfOrNull { it.versionCode } ?: 0
        if (BuildConfig.VERSION_CODE <= newestCode) return items
        return listOf(devBuildItem()) + items
    }

    private fun devBuildItem(): ChangelogItem {
        val iso = BuildConfig.BUILD_TIME_ISO
        return ChangelogItem(
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            dateIso = iso.take(10),
            bulletsEn = listOf(
                "Running ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE}). Built at $iso.",
            ),
        )
    }
}
