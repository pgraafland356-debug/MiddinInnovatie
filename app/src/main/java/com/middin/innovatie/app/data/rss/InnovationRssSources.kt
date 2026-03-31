package com.middin.innovatie.app.data.rss

/**
 * RSS sources focused on digital health, devices, and industry innovation.
 * Every item is still filtered for health context + innovation signals and for recency in code.
 */
data class InnovationRssSource(
    val url: String,
    val sourceLabel: String,
)

object InnovationRssSources {
    val feeds: List<InnovationRssSource> = listOf(
        InnovationRssSource(
            url = "https://www.mobihealthnews.com/feed/",
            sourceLabel = "MobiHealthNews",
        ),
        InnovationRssSource(
            url = "https://www.digitalhealth.net/feed/",
            sourceLabel = "Digital Health",
        ),
        InnovationRssSource(
            url = "https://www.medicaldevice-network.com/feed/",
            sourceLabel = "Medical Device Network",
        ),
        InnovationRssSource(
            url = "https://www.fiercehealthcare.com/rss/xml",
            sourceLabel = "Fierce Healthcare",
        ),
    )
}
