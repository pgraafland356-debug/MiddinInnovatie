package com.middin.innovatie.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogJsonTest {
    @Test
    fun changelogUrl_replaces_latest_json() {
        val url = "https://raw.githubusercontent.com/o/r/main/releases/latest.json"
        assertEquals(
            "https://raw.githubusercontent.com/o/r/main/releases/changelog.json",
            ChangelogJson.changelogUrlFromUpdateFeed(url),
        )
    }

    @Test
    fun parse_entries_sorted_by_merge() {
        val json = """
            {
              "entries": [
                {
                  "versionCode": 2,
                  "versionName": "0.2",
                  "dateIso": "2026-03-24",
                  "bullets": ["Second"]
                },
                {
                  "versionCode": 1,
                  "versionName": "0.1",
                  "dateIso": "2026-03-24",
                  "bullets": ["First"]
                }
              ]
            }
        """.trimIndent()
        val items = ChangelogJson.toItems(ChangelogJson.parse(json))
        assertEquals(2, items.size)
        assertEquals(2, items[0].versionCode)
        assertEquals("Second", items[0].bulletsEn.single())
    }

    @Test
    fun merge_remote_overrides_bundled_for_same_code() {
        val bundled = listOf(
            ChangelogJson.Entry(1, "0.1", "2026-01-01", listOf("bundled")),
        )
        val remote = listOf(
            ChangelogJson.Entry(1, "0.1", "2026-01-02", listOf("remote")),
        )
        val merged = ChangelogJson.merge(remote, bundled)
        assertEquals("remote", merged.single().bulletsEn.single())
    }

    @Test
    fun bundled_changelog_asset_is_not_empty() {
        val stream = javaClass.classLoader.getResourceAsStream("changelog.json")
        assertTrue(stream != null)
        val body = stream!!.bufferedReader().use { it.readText() }
        assertTrue(ChangelogJson.parse(body).isNotEmpty())
    }
}
