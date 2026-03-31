package com.middin.innovatie.app.data.rss

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object RssPubDateParser {

    private val formatters: List<DateTimeFormatter> = listOf(
        DateTimeFormatter.RFC_1123_DATE_TIME,
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ISO_ZONED_DATE_TIME,
    )

    fun parseToEpochMillis(raw: String?, fallbackMs: Long): Long {
        if (raw.isNullOrBlank()) return fallbackMs
        val cleaned = raw.trim()
        for (f in formatters) {
            try {
                return ZonedDateTime.parse(cleaned, f).toInstant().toEpochMilli()
            } catch (_: Exception) {
                // try next
            }
        }
        return fallbackMs
    }

    /** Used for recency: missing or unparseable dates are excluded from the feed. */
    fun parseToEpochMillisOrNull(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.trim()
        for (f in formatters) {
            try {
                return ZonedDateTime.parse(cleaned, f).toInstant().toEpochMilli()
            } catch (_: Exception) {
                // try next
            }
        }
        return null
    }
}
