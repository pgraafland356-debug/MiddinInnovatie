package com.middin.innovatie.app.data.local

/**
 * Default catalog: names + korte beschrijvingen (publieke productinformatie).
 * De Muse, Speciale muis/balmuis en digitaal chat app bewust zonder tekst.
 *
 * Bronnen o.a.: fabrikanten (Somnox, Luvion, MOWOOT, OrCam, handSteady, CRDL, Tinybots),
 * medische/registratiesites, Inclusive Inc. (OnPoint), MijnEigenPlan.nl, algemene actigrafie-info MotionWatch.
 */
object ProductCatalogSeed {

    data class Entry(val name: String, val description: String)

    val catalog: List<Entry> = listOf(
        Entry(
            name = "De Somnox",
            description = "Zachte slaaprobot die een rustig ademritme nabootst. " +
                "Door hem vast te houden stemt je ademhaling vaak mee, wat ontspanning en inslapen kan helpen. " +
                "Instellingen via de Somnox-app (o.a. adempo-efeningen).",
        ),
        Entry(
            name = "De Motionwatch",
            description = "Polsactigraaf (bekend als MotionWatch 8) voor slapen, activiteit en circadiaans ritme. " +
                "Bevat een versnellingsmeter en vaak een lichtsensor; data-export voor zorg en onderzoek. " +
                "CE-gemarkeerd medisch hulpmiddel in deze productlijn.",
        ),
        Entry(
            name = "De Tessa",
            description = "Kleine zorgrobot van Tinybots (Nederland): gesproken herinneringen, stappenplannen en " +
                "bevestigingsvragen. Begeleiders plannen via het web; o.a. bij dementie, NAH, ASS en " +
                "verstandelijke beperking. Ruim ingezet in de wijkverpleging.",
        ),
        Entry(
            name = "De luvion White noise speaker / Sleep assistant",
            description = "Luvion slaap- en witte-ruishulp met tientallen kalmerende geluiden " +
                "(hartslag, natuur, regen, ventilator, enz.), timers en USB-opladen. " +
                "Maskeert omgevingsgeluid; geschikt vanaf geboorte en ook voor volwassenen.",
        ),
        Entry(
            name = "De Mowoot 1",
            description = "MOWOOT: medisch hulpmiddel bij chronische obstipatie door zachte pneumatische " +
                "buikmassage (darmperistalse stimuleren). Korte dagelijkse sessies; niet-medicamenteus, CE klasse IIa.",
        ),
        Entry(
            name = "Mijn eigen plan app",
            description = "MijnEigenPlan: app en platform voor dag-/weekoverzicht, stappenplannen, herinneringen " +
                "en emotieregulatie. Voor cliënten en begeleiders in o.a. gehandicaptenzorg, GGZ en speciaal onderwijs " +
                "(mijneigenplan.nl).",
        ),
        Entry(
            name = "De launcher app",
            description = "Vereenvoudigde Android-start Scherm: grote pictogrammen, weinig gebaren, vaak SOS of " +
                "contacten op één tik. Voorbeelden: BIG Launcher, Elder Launcher, Senior Home—handig bij " +
                "cognitieve of visuele beperking.",
        ),
        Entry(
            name = "De VR-bril",
            description = "Virtualreality-bril voor therapie, training, ontspanning of exposure-oefeningen. " +
                "In zorg soms voor motoriek of cognitie; duur en duizeligheid beperken—afstemmen met zorgverlener.",
        ),
        Entry(
            name = "De CRDL",
            description = "CRDL (‘cradle’): houten muziekinstrument waarbij aanraking tussen twee mensen via " +
                "contactpunten muziek maakt. Gericht op contact en welbevinden bij o.a. ernstige dementie en apathie.",
        ),
        Entry(
            name = "De Orcam",
            description = "OrCam: kleine camera op de bril die tekst hardop voorleest, gezichten en voorwerpen " +
                "herkent. Werkt grotendeels offline; bedoeld voor slechtzienden en blinden.",
        ),
        Entry(
            name = "De handsteady",
            description = "handSteady-drinkbeker met draaigreep en deksel: de inhoud blijft vlakker bij tremor " +
                "(Parkinson, essentiële tremor). Lichte, herbruikbare beker, vaak vaatwasmachinebestendig.",
        ),
        Entry(
            name = "De Braintrainer",
            description = "Cognitieve training via apps (geheugen, aandacht, snelheid), bijv. CogniFit of NeuroNation " +
                "met persoonlijke schema’s. Kan ondersteunen bij training; geen vervanging van medisch onderzoek.",
        ),
        Entry(
            name = "De Onpoint tril stylus",
            description = "OnPoint (Inclusive Inc.): precisie-joystick als muis of met Microsoft Adaptive Controller " +
                "voor fijne aansturing bij beperkte motoriek. Apart bestaan trillende pennen voor o.a. Parkinson; " +
                "dit product is vooral joystick/pointerfunctionaliteit.",
        ),
        Entry(name = "De Muse", description = ""),
        Entry(name = "De Speciale muis / bal muis", description = ""),
        Entry(name = "De digitaal chat app", description = ""),
    )

    /** Volgorde zoals in de UI gewenst (boven = eerst in lijst bij sorteerdatum). */
    val defaultProductNames: List<String> = catalog.map { it.name }

    suspend fun seedIfEmpty(dao: ProductDao) {
        if (dao.count() > 0) return
        val base = System.currentTimeMillis()
        catalog.reversed().forEachIndexed { index, entry ->
            dao.insert(
                Product(
                    name = entry.name,
                    description = entry.description,
                    imageUri = null,
                    createdAtEpochMs = base + index,
                ),
            )
        }
    }

    /** Vult lege beschrijvingen bij bestaande catalogusregels (bijv. na eerdere app-versie). */
    suspend fun fillEmptyDescriptions(dao: ProductDao) {
        catalog.forEach { entry ->
            if (entry.description.isNotBlank()) {
                dao.updateDescriptionIfEmpty(entry.name, entry.description)
            }
        }
    }
}
