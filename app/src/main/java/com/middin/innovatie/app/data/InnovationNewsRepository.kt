package com.middin.innovatie.app.data

import com.middin.innovatie.app.data.rss.InnovationNewsKeywordFilter
import com.middin.innovatie.app.data.rss.InnovationRssSources
import com.middin.innovatie.app.data.rss.RssParser
import com.middin.innovatie.app.data.rss.RssPubDateParser
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

/**
 * Loads care-innovation headlines from curated RSS feeds (with keyword filtering where needed).
 * Falls back to a static list when the network fails or all feeds are empty.
 */
class InnovationNewsRepository(
    private val httpClient: HttpClient,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) {

    private val zone: ZoneId = ZoneId.of("Europe/Amsterdam")
    private val dateDisplayNl: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("nl-NL")).withZone(zone)
    private val dateDisplayEn: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH).withZone(zone)

    /**
     * Shown until RSS finishes loading; also used when RSS cannot be loaded.
     */
    fun fallbackFeed(): List<InnovationNewsItem> = staticFallbackItems()

    suspend fun loadFeed(localeForDates: Locale = Locale.getDefault()): List<InnovationNewsItem> =
        withContext(ioContext) {
            val fromRss = fetchMergedRss(localeForDates)
            if (fromRss.isNotEmpty()) fromRss else staticFallbackItems()
        }

    private suspend fun fetchMergedRss(localeForDates: Locale): List<InnovationNewsItem> = coroutineScope {
        val now = System.currentTimeMillis()
        InnovationRssSources.feeds
            .map { source ->
                async {
                    runCatching { fetchAndMap(source.url, source.sourceLabel, localeForDates, now) }
                        .getOrElse { emptyList() }
                }
            }
            .awaitAll()
            .flatten()
            .distinctBy { normalizeUrl(it.articleUrl) }
            .filter { it.sortEpochMs >= now - maxItemAgeMs }
            .sortedByDescending { it.sortEpochMs }
            .take(MAX_ITEMS)
    }

    private suspend fun fetchAndMap(
        url: String,
        sourceLabel: String,
        localeForDates: Locale,
        now: Long,
    ): List<InnovationNewsItem> {
        val xml = httpClient.get(url) {
            header(HttpHeaders.UserAgent, USER_AGENT)
        }.bodyAsText()
        val items = RssParser.parseItems(xml)
        val formatter = if (localeForDates.language.startsWith("nl")) dateDisplayNl else dateDisplayEn
        return items.mapNotNull { parsed ->
            val title = parsed.title
            val link = parsed.link
            if (title.isBlank() || !link.startsWith("https://")) return@mapNotNull null
            val summaryRaw = parsed.descriptionPlain
            val summaryShort = summaryRaw.take(SUMMARY_MAX).let { if (summaryRaw.length > SUMMARY_MAX) "$it…" else it }
            if (!InnovationNewsKeywordFilter.matchesTitleAndSummary(title, summaryRaw)) {
                return@mapNotNull null
            }
            val sortMs = RssPubDateParser.parseToEpochMillisOrNull(parsed.pubDateRaw) ?: return@mapNotNull null
            if (sortMs > now + ALLOW_FUTURE_SKEW_MS) return@mapNotNull null
            val dateLabel = formatter.format(Instant.ofEpochMilli(sortMs))
            InnovationNewsItem(
                id = "${sourceLabel}-${link.hashCode()}",
                title = title,
                summary = summaryShort.ifBlank { title },
                dateLabel = dateLabel,
                sortEpochMs = sortMs,
                articleUrl = link,
                sourceLabel = sourceLabel,
            )
        }
    }

    private fun normalizeUrl(url: String): String =
        url.substringBefore("#").trimEnd('/').lowercase()

    private fun staticFallbackItems(): List<InnovationNewsItem> {
        fun epoch(year: Int, month: Int, day: Int): Long =
            java.time.LocalDate.of(year, month, day).atStartOfDay(zone).toInstant().toEpochMilli()

        return listOf(
            InnovationNewsItem(
                id = "nl-ehealth",
                title = "E-health en digitale zorg (Rijksoverheid)",
                summary = "Overzicht van beleid rond digitale zorg, dossierdeling en wetgeving in Nederland. Handig om te begrijpen wat er speelt rond apps en gegevensuitwisseling.",
                dateLabel = "april 2026",
                sortEpochMs = epoch(2026, 4, 20),
                articleUrl = "https://www.rijksoverheid.nl/onderwerpen/e-health",
                sourceLabel = "Rijksoverheid.nl",
            ),
            InnovationNewsItem(
                id = "vilans",
                title = "Vilans: kennis voor langdurige zorg",
                summary = "Onderzoek, praktijkvoorbeelden en richtlijnen over kwaliteit en innovatie in de langdurige zorg — waaronder technologie en organisatie van zorg.",
                dateLabel = "april 2026",
                sortEpochMs = epoch(2026, 4, 8),
                articleUrl = "https://www.vilans.nl/",
                sourceLabel = "Vilans",
            ),
            InnovationNewsItem(
                id = "zonmw-innovatie",
                title = "ZonMw: onderzoek en innovatie in de zorg",
                summary = "Programma’s en subsidies voor zorginnovatie en onderzoek. Relevant als je wilt weten welke richtingen (o.a. technologie) in NL worden gefinancierd.",
                dateLabel = "maart 2026",
                sortEpochMs = epoch(2026, 3, 25),
                articleUrl = "https://www.zonmw.nl/nl/onderzoek-projecten/innovatie/",
                sourceLabel = "ZonMw",
            ),
            InnovationNewsItem(
                id = "nvwa-md",
                title = "Medische hulpmiddelen en veiligheid (NVWA)",
                summary = "Wie toezicht houdt op medische hulpmiddelen in Nederland en waar je basisinformatie vindt over regels, meldingen en markttoezicht.",
                dateLabel = "maart 2026",
                sortEpochMs = epoch(2026, 3, 12),
                articleUrl = "https://www.nvwa.nl/onderwerpen/medische-hulpmiddelen",
                sourceLabel = "NVWA",
            ),
            InnovationNewsItem(
                id = "who-md",
                title = "WHO: medische hulpmiddelen wereldwijd",
                summary = "Achtergrond over toegang tot veilige medische hulpmiddelen en internationale aandacht voor regulering en kwaliteit.",
                dateLabel = "februari 2026",
                sortEpochMs = epoch(2026, 2, 28),
                articleUrl = "https://www.who.int/health-topics/medical-devices",
                sourceLabel = "WHO",
            ),
            InnovationNewsItem(
                id = "eu-md-sector",
                title = "Europese Commissie: medische hulpmiddelen",
                summary = "Europese informatie over regels, MDR en de sector — nuttig naast Nederlandse instanties als je met CE-markering of import te maken hebt.",
                dateLabel = "februari 2026",
                sortEpochMs = epoch(2026, 2, 14),
                articleUrl = "https://health.ec.europa.eu/medical-devices-sector_en",
                sourceLabel = "European Commission",
            ),
            InnovationNewsItem(
                id = "tinybots",
                title = "Tinybots: zorgrobot Tessa (Nederlands bedrijf)",
                summary = "Herinnerings- en begeleidingsrobot voor thuis en zorginstellingen; voorbeeld van NL-zorgtech die breed in de wijk wordt ingezet.",
                dateLabel = "januari 2026",
                sortEpochMs = epoch(2026, 1, 30),
                articleUrl = "https://www.tinybots.nl/",
                sourceLabel = "Tinybots",
            ),
            InnovationNewsItem(
                id = "somnox",
                title = "Somnox: slaaprobot en rust",
                summary = "Consumentenproduct (slaapcompanion) dat via ademritme ontspanning ondersteunt; vaak genoemd in slaap- en zorginnovatie-context.",
                dateLabel = "januari 2026",
                sortEpochMs = epoch(2026, 1, 18),
                articleUrl = "https://www.somnox.com/nl/",
                sourceLabel = "Somnox",
            ),
            InnovationNewsItem(
                id = "mijneigenplan",
                title = "MijnEigenPlan: dagstructuur en ondersteuning",
                summary = "Platform en app voor plannen, pictogrammen en communicatie tussen cliënt en begeleiders — veel gebruikt in NL zorg en onderwijs.",
                dateLabel = "december 2025",
                sortEpochMs = epoch(2025, 12, 20),
                articleUrl = "https://mijneigenplan.nl/",
                sourceLabel = "MijnEigenPlan",
            ),
            InnovationNewsItem(
                id = "alzheimer-nl",
                title = "Alzheimer Nederland: leven met dementie",
                summary = "Informatie en steun voor mensen met dementie en mantelzorgers; ook aandacht voor omgeving, daginvulling en soms hulpmiddelen.",
                dateLabel = "december 2025",
                sortEpochMs = epoch(2025, 12, 5),
                articleUrl = "https://www.alzheimer-nederland.nl/",
                sourceLabel = "Alzheimer Nederland",
            ),
            InnovationNewsItem(
                id = "eic-health",
                title = "European Innovation Council (EIC)",
                summary = "Europese financiering en accelerator voor innovatieve bedrijven, waaronder gezondheid en life sciences — relevant voor start-ups in zorgtech.",
                dateLabel = "november 2025",
                sortEpochMs = epoch(2025, 11, 20),
                articleUrl = "https://eic.ec.europa.eu/eic-funding-opportunities_en",
                sourceLabel = "European Innovation Council",
            ),
            InnovationNewsItem(
                id = "digital-europe-ai",
                title = "EU: AI-wetgeving (overzicht)",
                summary = "Kader voor artificiële intelligentie in de EU; raakt ook medische toepassingen en classificatie van ‘hoog risico’-systemen.",
                dateLabel = "november 2025",
                sortEpochMs = epoch(2025, 11, 5),
                articleUrl = "https://digital-strategy.ec.europa.eu/en/policies/regulatory-framework-ai",
                sourceLabel = "European Commission",
            ),
        ).sortedByDescending { it.sortEpochMs }
    }

    private val maxItemAgeMs: Long = Duration.ofDays(MAX_ITEM_AGE_DAYS).toMillis()

    companion object {
        private const val MAX_ITEMS = 24
        private const val MAX_ITEM_AGE_DAYS = 45L
        private const val ALLOW_FUTURE_SKEW_MS = 86_400_000L
        private const val SUMMARY_MAX = 420
        private const val USER_AGENT = "MiddinInnovatie/1.0 (Android; zorginnovatie RSS)"
    }
}
