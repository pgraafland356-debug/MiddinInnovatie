package com.middin.innovatie.app.data

import java.time.LocalDate
import java.time.ZoneId

/**
 * Static feed of recent care-innovation pointers. Updates ship with app releases.
 * Summaries are editorial; always verify on the source site (CE, MDR, procurement).
 */
class InnovationNewsRepository {

    private val zone: ZoneId = ZoneId.of("Europe/Amsterdam")

    private fun epoch(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day).atStartOfDay(zone).toInstant().toEpochMilli()

    fun getFeed(): List<InnovationNewsItem> {
        return listOf(
            InnovationNewsItem(
                id = "nl-ehealth",
                title = "E-health en digitale zorg (Rijksoverheid)",
                summary = "Overzicht van beleid rond digitale zorg, dossierdeling en wetgeving in Nederland. Handig om te begrijpen wat er speelt rond apps en gegevensuitwisseling.",
                dateLabel = "maart 2025",
                sortEpochMs = epoch(2025, 3, 1),
                articleUrl = "https://www.rijksoverheid.nl/onderwerpen/e-health",
                sourceLabel = "Rijksoverheid.nl",
            ),
            InnovationNewsItem(
                id = "vilans",
                title = "Vilans: kennis voor langdurige zorg",
                summary = "Onderzoek, praktijkvoorbeelden en richtlijnen over kwaliteit en innovatie in de langdurige zorg — waaronder technologie en organisatie van zorg.",
                dateLabel = "februari 2025",
                sortEpochMs = epoch(2025, 2, 15),
                articleUrl = "https://www.vilans.nl/",
                sourceLabel = "Vilans",
            ),
            InnovationNewsItem(
                id = "zonmw-innovatie",
                title = "ZonMw: onderzoek en innovatie in de zorg",
                summary = "Programma’s en subsidies voor zorginnovatie en onderzoek. Relevant als je wilt weten welke richtingen (o.a. technologie) in NL worden gefinancierd.",
                dateLabel = "februari 2025",
                sortEpochMs = epoch(2025, 2, 1),
                articleUrl = "https://www.zonmw.nl/nl/onderzoek-projecten/innovatie/",
                sourceLabel = "ZonMw",
            ),
            InnovationNewsItem(
                id = "nvwa-md",
                title = "Medische hulpmiddelen en veiligheid (NVWA)",
                summary = "Wie toezicht houdt op medische hulpmiddelen in Nederland en waar je basisinformatie vindt over regels, meldingen en markttoezicht.",
                dateLabel = "januari 2025",
                sortEpochMs = epoch(2025, 1, 20),
                articleUrl = "https://www.nvwa.nl/onderwerpen/medische-hulpmiddelen",
                sourceLabel = "NVWA",
            ),
            InnovationNewsItem(
                id = "who-md",
                title = "WHO: medische hulpmiddelen wereldwijd",
                summary = "Achtergrond over toegang tot veilige medische hulpmiddelen en internationale aandacht voor regulering en kwaliteit.",
                dateLabel = "januari 2025",
                sortEpochMs = epoch(2025, 1, 10),
                articleUrl = "https://www.who.int/health-topics/medical-devices",
                sourceLabel = "WHO",
            ),
            InnovationNewsItem(
                id = "eu-md-sector",
                title = "Europese Commissie: medische hulpmiddelen",
                summary = "Europese informatie over regels, MDR en de sector — nuttig naast Nederlandse instanties als je met CE-markering of import te maken hebt.",
                dateLabel = "december 2024",
                sortEpochMs = epoch(2024, 12, 5),
                articleUrl = "https://health.ec.europa.eu/medical-devices-sector_en",
                sourceLabel = "European Commission",
            ),
            InnovationNewsItem(
                id = "tinybots",
                title = "Tinybots: zorgrobot Tessa (Nederlands bedrijf)",
                summary = "Herinnerings- en begeleidingsrobot voor thuis en zorginstellingen; voorbeeld van NL-zorgtech die breed in de wijk wordt ingezet.",
                dateLabel = "november 2024",
                sortEpochMs = epoch(2024, 11, 18),
                articleUrl = "https://www.tinybots.nl/",
                sourceLabel = "Tinybots",
            ),
            InnovationNewsItem(
                id = "somnox",
                title = "Somnox: slaaprobot en rust",
                summary = "Consumentenproduct (slaapcompanion) dat via ademritme ontspanning ondersteunt; vaak genoemd in slaap- en zorginnovatie-context.",
                dateLabel = "november 2024",
                sortEpochMs = epoch(2024, 11, 1),
                articleUrl = "https://www.somnox.com/nl/",
                sourceLabel = "Somnox",
            ),
            InnovationNewsItem(
                id = "mijneigenplan",
                title = "MijnEigenPlan: dagstructuur en ondersteuning",
                summary = "Platform en app voor plannen, pictogrammen en communicatie tussen cliënt en begeleiders — veel gebruikt in NL zorg en onderwijs.",
                dateLabel = "oktober 2024",
                sortEpochMs = epoch(2024, 10, 12),
                articleUrl = "https://mijneigenplan.nl/",
                sourceLabel = "MijnEigenPlan",
            ),
            InnovationNewsItem(
                id = "alzheimer-nl",
                title = "Alzheimer Nederland: leven met dementie",
                summary = "Informatie en steun voor mensen met dementie en mantelzorgers; ook aandacht voor omgeving, daginvulling en soms hulpmiddelen.",
                dateLabel = "oktober 2024",
                sortEpochMs = epoch(2024, 10, 1),
                articleUrl = "https://www.alzheimer-nederland.nl/",
                sourceLabel = "Alzheimer Nederland",
            ),
            InnovationNewsItem(
                id = "eic-health",
                title = "European Innovation Council (EIC)",
                summary = "Europese financiering en accelerator voor innovatieve bedrijven, waaronder gezondheid en life sciences — relevant voor start-ups in zorgtech.",
                dateLabel = "september 2024",
                sortEpochMs = epoch(2024, 9, 15),
                articleUrl = "https://eic.ec.europa.eu/eic-funding-opportunities_en",
                sourceLabel = "European Innovation Council",
            ),
            InnovationNewsItem(
                id = "digital-europe-ai",
                title = "EU: AI-wetgeving (overzicht)",
                summary = "Kader voor artificiële intelligentie in de EU; raakt ook medische toepassingen en classificatie van ‘hoog risico’-systemen.",
                dateLabel = "augustus 2024",
                sortEpochMs = epoch(2024, 8, 20),
                articleUrl = "https://digital-strategy.ec.europa.eu/en/policies/regulatory-framework-ai",
                sourceLabel = "European Commission",
            ),
        ).sortedByDescending { it.sortEpochMs }
    }
}
