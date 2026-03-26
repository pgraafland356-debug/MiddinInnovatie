package com.middin.innovatie.app.data

/**
 * Curated headline for the home newsfeed (care tech & zorginnovatie).
 * [sortEpochMs] orders newest first; [dateLabel] is shown to the user.
 */
data class InnovationNewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val dateLabel: String,
    val sortEpochMs: Long,
    val articleUrl: String,
    val sourceLabel: String,
)
