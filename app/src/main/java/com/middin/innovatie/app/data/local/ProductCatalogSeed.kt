package com.middin.innovatie.app.data.local

/**
 * Default catalog: names + korte beschrijvingen (publieke productinformatie).
 *
 * Bronnen o.a.: fabrikanten (Somnox, Luvion, MOWOOT, OrCam, handSteady, CRDL, Tinybots, Muse, Abilia),
 * medische/registratiesites, Inclusive Inc. (OnPoint), MijnEigenPlan.nl, actigrafie MotionWatch,
 * Stichting Visitaal / visitaal.nl, Kennisplein Gehandicaptenzorg (Visitaal Chat), BIG Launcher (biglauncher.com, o.a. Visio kennisportaal).
 */
object ProductCatalogSeed {

    data class Entry(val name: String, val description: String)

    /** Oude seed-namen; catalogus is hernoemd of opgesplitst. */
    private val obsoleteProductNames: List<String> = listOf(
        "De digitaal chat app",
        "De Speciale muis / bal muis",
        "Qtronix",
    )

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
            description = "Vereenvoudigde Android-startschermen: grote pictogrammen, weinig gebaren, vaak SOS of " +
                "contacten op één tik (los van de aparte vermelding BIG Launcher). Voorbeelden: Elder Launcher, " +
                "Senior Home, Simple Launcher — handig bij cognitieve of visuele beperking.",
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
        Entry(
            name = "De Muse",
            description = "Muse is een draagbare hoofdband met EEG-sensoren die hersenactiviteit meet tijdens " +
                "meditatie en ontspanning. De bijbehorende app geeft biofeedback (geluid of beeld) zodat je merkt " +
                "wanneer je geest rustiger of juist actiever is. Bedoeld voor welzijn en training van aandacht, " +
                "geen medische diagnose van hersenaandoeningen (bron: Muse / Choose Muse).",
        ),
        Entry(
            name = "Qtronix Libra 90 net",
            description = "De Qtronix Libra 90 is een USB-trackball (balmuis) uit de Libra-serie: een grote rolbal " +
                "voor duim of vingers, meerdere knoppen en vaak een scrollwiel — minimale polsbeweging bij aansturing, " +
                "prettig bij RSI-klachten, tremor of beperkte arm-/handfunctie. Geschikt op bureau of rolstoeltafel; " +
                "bedrade USB-aansluiting (meestal type-A; met adapter eventueel type-C). Installeer eventuele drivers " +
                "alleen via de leverancier of fabrikant Qtronix. Geen medisch hulpmiddel — bij twijfel: overleg met " +
                "een ergotherapeut.",
        ),
        Entry(
            name = "Visitaal Chat",
            description = "Gratis Nederlandse chat-app met duidelijke pictogrammen in plaats van veel tekst — " +
                "begrijpelijk voor wie moeite heeft met lezen en schrijven, vergelijkbaar in doel met een toegankelijk " +
                "alternatief voor standaard chatapps. Ruim 350 pictogrammen; ook foto's, emoji's, GIF's en spraakberichten; " +
                "berichten kunnen hardop worden voorgelezen. Bedoeld o.a. voor mensen met een (lichte) verstandelijke of " +
                "communicatieve beperking, auditieve beperking, dementie, anderstaligen en laaggeletterden. " +
                "Beschikbaar voor Android en iOS (telefoon en tablet); in de praktijk vaak via wifi. " +
                "(Stichting Visitaal — visitaal.nl; o.a. beschreven op Kennisplein Gehandicaptenzorg.)",
        ),
        Entry(
            name = "BIG Launcher",
            description = "Android-app die het standaard startscherm vervangt door een eenvoudige interface met " +
                "zeer grote knoppen en tekst: minder fouten en overzicht voor senioren, slechtzienden en mensen met " +
                "motorische beperkingen. Aanpasbare tekstgroottes en kleurenthema's; vaak een SOS-functie " +
                "(waaronder locatie delen). Basis gratis, uitbreidingen optioneel betaald — zie biglauncher.com en " +
                "Google Play (o.a. toegelicht door Koninklijke Visio in het kennisportaal).",
        ),
        Entry(
            name = "MEMO Timer van Abilia",
            description = "MEMO Timer van Abilia: draagbare visuele timer die tijd zichtbaar maakt — LED-stippen in " +
                "een lichtzuil doven geleidelijk uit naarmate de tijd verstrijkt, zodat iets abstracts als 'een half uur' " +
                "concreet wordt. Bij het einde flitsen de LED's; optioneel met geluidssignaal en trilling. " +
                "Waterdicht (IP67), geschikt voor badkamer, douche of buiten. Vier varianten met vaste tijdsintervallen: " +
                "MEMO Timer 8 (2/4/6/8 min), MEMO Timer 20 (5/10/15/20 min), MEMO Timer 60 (15/30/45/60 min) en " +
                "MEMO Timer 80 (20/40/60/80 min). Inclusief pols- en nekkoord; werkt op 2× AA-batterijen. " +
                "Hulpmiddel klasse I voor mensen met een beperking die ondersteuning nodig hebben bij tijdsbesef en " +
                "planning (o.a. douchen, activiteiten, overgangen tussen taken). Bron: abilia.com.",
        ),
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

    /** Verwijdert verouderde seed-producten (na hernoemen/splitsen in de catalogus). */
    suspend fun removeObsoleteProducts(dao: ProductDao) {
        obsoleteProductNames.forEach { dao.deleteByName(it) }
    }

    /**
     * Voegt ontbrekende catalogusproducten toe (bestaande installaties na uitbreiding van [catalog]).
     * [seedIfEmpty] draait alleen bij lege DB; deze functie vult bij iedere app-start het verschil aan.
     */
    suspend fun syncMissingCatalogEntries(dao: ProductDao) {
        val base = System.currentTimeMillis()
        catalog.forEachIndexed { index, entry ->
            if (dao.countByName(entry.name) == 0L) {
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
    }
}
