package com.middin.innovatie.app

import org.json.JSONArray
import org.json.JSONObject

data class ChangelogItem(
    val versionCode: Int,
    val versionName: String?,
    val dateIso: String,
    val bulletsEn: List<String>,
) {
    val displayLabel: String
        get() = if (!versionName.isNullOrBlank()) {
            "$versionName ($versionCode)"
        } else {
            versionCode.toString()
        }
}

object ChangelogJson {

    data class Entry(
        val versionCode: Int,
        val versionName: String?,
        val dateIso: String,
        val bullets: List<String>,
    )

    fun changelogUrlFromUpdateFeed(updateFeedUrl: String): String {
        val trimmed = updateFeedUrl.trim()
        if (trimmed.isEmpty()) return ""
        val marker = "latest.json"
        val idx = trimmed.lastIndexOf(marker, ignoreCase = true)
        return if (idx >= 0) {
            trimmed.substring(0, idx) + "changelog.json"
        } else {
            trimmed.trimEnd('/') + "/changelog.json"
        }
    }

    fun parse(jsonBody: String): List<Entry> {
        val root = JSONObject(jsonBody.trim().removePrefix("\uFEFF"))
        val entries = root.optJSONArray("entries") ?: JSONArray()
        return buildList {
            for (i in 0 until entries.length()) {
                val obj = entries.getJSONObject(i)
                val bullets = obj.optJSONArray("bullets") ?: JSONArray()
                add(
                    Entry(
                        versionCode = obj.getInt("versionCode"),
                        versionName = obj.optString("versionName").takeIf { it.isNotBlank() },
                        dateIso = obj.optString("dateIso", ""),
                        bullets = buildList {
                            for (b in 0 until bullets.length()) {
                                val line = bullets.optString(b).trim()
                                if (line.isNotEmpty()) add(line)
                            }
                        },
                    ),
                )
            }
        }
    }

    fun toItems(entries: List<Entry>): List<ChangelogItem> =
        entries
            .sortedByDescending { it.versionCode }
            .map { entry ->
                ChangelogItem(
                    versionCode = entry.versionCode,
                    versionName = entry.versionName,
                    dateIso = entry.dateIso,
                    bulletsEn = entry.bullets,
                )
            }

    fun merge(remote: List<Entry>, bundled: List<Entry>): List<ChangelogItem> {
        val byCode = linkedMapOf<Int, Entry>()
        bundled.forEach { byCode[it.versionCode] = it }
        remote.forEach { byCode[it.versionCode] = it }
        return toItems(byCode.values.toList())
    }
}
