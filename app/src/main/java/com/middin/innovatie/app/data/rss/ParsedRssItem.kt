package com.middin.innovatie.app.data.rss

/** One entry from an RSS 2.0 channel before mapping to [InnovationNewsItem]. */
data class ParsedRssItem(
    val title: String,
    val link: String,
    val descriptionPlain: String,
    val pubDateRaw: String?,
)
