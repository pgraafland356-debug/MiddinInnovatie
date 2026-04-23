package com.middin.innovatie.app.data.local

import java.util.Locale

/**
 * Antwoorden op basis van producten in de app: eenvoudige taal, duidelijke alinea’s.
 * Antwoorden zijn altijd Nederlands. Werkt volledig offline (geen netwerk).
 */
object ProductChatKnowledge {

    private val NL: Locale = Locale.forLanguageTag("nl-NL")

    private val STOP = setOf(
        "de", "het", "een", "en", "van", "voor", "met", "naar", "over", "wat", "wie", "hoe", "waar",
        "kan", "kun", "kunnen", "wil", "willen", "weten", "graag", "als", "maar", "ook", "nog", "niet",
        "die", "dat", "dit", "zijn", "is", "ben", "bij", "uit", "aan", "er", "te", "om", "of", "zo",
        "eens", "even", "mag", "mijn", "jouw", "hier", "daar", "waarom", "doet", "help", "helpt",
        "iets", "meer", "veel",
        // Engels (antwoorden blijven NL; vragen kunnen Engels zijn)
        "what", "which", "when", "where", "why", "how", "who", "the", "you", "your", "have", "has",
        "does", "did", "can", "could", "would", "should", "please", "tell", "about", "me", "any",
    )

    fun answer(question: String, products: List<Product>): String {
        val q = question.trim()
        if (q.isEmpty()) return friendlyNoMatch(products)

        val lower = normalize(q)
        if (isWhoAreYouQuestion(lower)) {
            return whoAreYouReply(products)
        }
        if (isHowCanYouHelpQuestion(lower)) {
            return howCanYouHelpReply(products)
        }
        if (isGreeting(lower)) {
            return greetingReply(products)
        }
        if (wantsProductList(lower)) {
            return formatProductList(products)
        }

        val qTokens = tokenize(q)
        if (products.isEmpty()) {
            return "Er staan nog geen producten in de app. Voeg eerst producten toe op het tabblad Producten."
        }

        val scored = products.map { p -> p to scoreProduct(qTokens, lower, p) }
        val best = scored.maxByOrNull { it.second }!!
        val thresholdStrong = 4
        val thresholdWeak = 1

        val strong = scored.filter { it.second >= thresholdStrong }.sortedByDescending { it.second }
        if (strong.size >= 2 && strong[0].second == strong[1].second && strong[0].second >= thresholdStrong) {
            return formatMultiAnswer(strong.take(3).map { it.first })
        }

        if (best.second >= thresholdStrong) {
            return formatSingleProduct(best.first)
        }
        if (best.second >= thresholdWeak) {
            return buildString {
                appendLine(formatSingleProduct(best.first))
                appendLine()
                append("(Als dit niet klopt, typ dan de naam van het product zoals in de lijst.)")
            }.trim()
        }
        return friendlyNoMatch(products)
    }

    private fun normalize(s: String): String =
        s.lowercase(NL)
            .replace('’', '\'')
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun isWhoAreYouQuestion(lower: String): Boolean {
        if ("wie ben jij" in lower || "wie ben je" in lower) return true
        if ("wie zij jij" in lower || "wie zij je" in lower) return true
        // e.g. "Kun jij me vertellen wie je bent?" — uses "wie je bent", not "wie ben je"
        if ("wie je bent" in lower || "wie jij bent" in lower) return true
        // "Vertel me wie je bent" / "Ik wil weten wie je bent"
        if (("vertel" in lower || "vertellen" in lower || "zeggen" in lower || "weten" in lower) &&
            "wie" in lower &&
            ("je bent" in lower || "jij bent" in lower || "ben je" in lower || "ben jij" in lower)
        ) {
            return true
        }
        if ("who are you" in lower) return true
        if ("tell me who you are" in lower) return true
        return false
    }

    private fun isHowCanYouHelpQuestion(lower: String): Boolean {
        if ("waarmee kan je mij helpen" in lower) return true
        if ("waarmee kun je mij helpen" in lower) return true
        if ("waarmee kan je me helpen" in lower) return true
        if ("waarmee kun je me helpen" in lower) return true
        if ("waarmee help je mij" in lower || "waarmee help je me" in lower) return true
        if ("hoe kan je mij helpen" in lower || "hoe kun je mij helpen" in lower) return true
        if ("hoe kan je me helpen" in lower || "hoe kun je me helpen" in lower) return true
        if ("met wat kan je mij helpen" in lower || "met wat kun je mij helpen" in lower) return true
        if ("what can you help" in lower && ("me" in lower || "with" in lower)) return true
        return false
    }

    private fun whoAreYouReply(products: List<Product>): String {
        val hint = if (products.isEmpty()) {
            "Als er producten in de app staan, kan ik je daar uitleg over geven. Voeg ze toe via het tabblad Producten."
        } else {
            "Vraag gerust over een product uit de lijst, of tik: «Welke producten hebben jullie?»."
        }
        return buildString {
            appendLine("Ik ben de Productassistent van deze Middin Innovatie-app.")
            appendLine()
            appendLine(
                "Ik ben geen mens, maar een hulp in de app. Ik gebruik de teksten bij de producten " +
                    "die hier staan en probeer je vragen daarop te beantwoorden — in rustig, " +
                    "begrijpelijk Nederlands en zonder internet.",
            )
            appendLine()
            append(hint)
        }.trim()
    }

    private fun howCanYouHelpReply(products: List<Product>): String {
        return buildString {
            appendLine("Ik kan je helpen met vragen over de producten in deze app.")
            appendLine()
            appendLine("Zo kun je me gebruiken:")
            appendLine("• Uitleg over wat een product doet of waarvoor het bedoeld is")
            appendLine("• De informatie in makkelijke stukjes en heldere taal")
            appendLine("• Een overzicht als je vraagt welke producten er zijn")
            appendLine()
            appendLine(
                "Ik gebruik alleen wat er bij elk product in de app staat. " +
                    "Voor medisch advies, vergoedingen of jouw persoonlijke situatie moet je " +
                    "altijd naar je begeleider, arts of de leverancier van het hulpmiddel.",
            )
            if (products.isEmpty()) {
                appendLine()
                appendLine(
                    "Er staan nog geen producten in de app. Voeg eerst producten toe; " +
                        "daarna kan ik je er meer over vertellen.",
                )
            }
            appendLine()
            appendLine("Typ gerust de naam van een product of stel een concrete vraag.")
        }.trim()
    }

    private fun isGreeting(lower: String): Boolean {
        val greetings = listOf(
            "hallo", "hoi", "hey", "hi", "goedemorgen", "goedemiddag", "goedenavond", "dag",
            "hello", "thanks", "bedankt", "dank",
        )
        if (lower.length <= 12 && greetings.any { lower == it || lower.startsWith("$it ") }) return true
        return greetings.any { lower.startsWith(it) } && lower.length < 40
    }

    private fun wantsProductList(lower: String): Boolean {
        if ("lijst" in lower || "overzicht" in lower) return true
        if ("alle" in lower && "product" in lower) return true
        if ("welke" in lower && "product" in lower) return true
        if ("wat heb" in lower || "wat zijn" in lower) return true
        if ("which" in lower && "product" in lower) return true
        if ("list" in lower && "product" in lower) return true
        if ("all" in lower && "product" in lower) return true
        if ("what" in lower && "product" in lower) return true
        return false
    }

    private fun tokenize(s: String): List<String> {
        return Regex("[a-zA-Zà-ëìòùáéíóúüßœæ0-9]+")
            .findAll(s.lowercase(NL))
            .map { it.value }
            .filter { it.length >= 2 && it !in STOP }
            .toList()
    }

    private fun scoreProduct(qTokens: List<String>, qLower: String, p: Product): Int {
        val nameLower = p.name.lowercase(NL)
        val descLower = p.description.lowercase(NL)
        var score = 0

        if (qLower.length >= 3 && nameLower.contains(qLower)) score += 25
        for (t in qTokens) {
            if (t.length >= 3 && nameLower.contains(t)) score += 10
            if (t.length >= 4 && descLower.contains(t)) score += 3
            for (part in nameLower.split(Regex("\\W+"))) {
                if (part.length >= 3 && (part == t || part.startsWith(t) || t.startsWith(part))) score += 6
            }
        }
        for (syn in productSynonyms(p.name)) {
            if (syn in qLower) score += 12
        }
        return score
    }

    private fun productSynonyms(name: String): List<String> {
        return when {
            name.contains("Somnox", ignoreCase = true) -> listOf("somnox", "slaaprobot", "slaap", "slapen", "inslapen")
            name.contains("Motionwatch", ignoreCase = true) -> listOf("motionwatch", "actigraaf", "pols", "slaapritme")
            name.contains("Tessa", ignoreCase = true) -> listOf("tessa", "tinybot", "robot")
            name.contains("Luvion", ignoreCase = true) || name.contains("White noise", ignoreCase = true) ->
                listOf("luvion", "witte", "ruis", "white", "noise", "geluid", "slaap", "slapen")
            name.contains("Mowoot", ignoreCase = true) -> listOf("mowoot", "buik", "obstipatie", "darm")
            name.contains("Mijn eigen plan", ignoreCase = true) -> listOf("mijneigenplan", "eigen", "plan")
            name.contains("Visitaal", ignoreCase = true) ->
                listOf("visitaal", "chat", "pictogrammen", "whatsapp", "bericht", "praten", "communicatie", "voorlezen")
            name.contains("BIG Launcher", ignoreCase = true) ->
                listOf("big", "launcher", "grote", "letters", "iconen", "start", "android", "senior", "sos")
            name.contains("launcher", ignoreCase = true) -> listOf("launcher", "grote", "iconen", "start")
            name.contains("VR", ignoreCase = true) || name.contains("vr-", ignoreCase = true) ->
                listOf("vr", "virtual", "bril")
            name.contains("CRDL", ignoreCase = true) -> listOf("crdl", "muziek", "aanraking")
            name.contains("Orcam", ignoreCase = true) -> listOf("orcam", "voorlezen", "blind", "slechtziend")
            name.contains("handsteady", ignoreCase = true) -> listOf("handsteady", "beker", "tremor", "trillen")
            name.contains("Braintrainer", ignoreCase = true) -> listOf("braintrainer", "cognitief", "hersenen", "training")
            name.contains("Onpoint", ignoreCase = true) || name.contains("tril stylus", ignoreCase = true) ->
                listOf("onpoint", "stylus", "joystick", "muis")
            name.contains("Muse", ignoreCase = true) -> listOf("muse", "eeg", "meditatie", "hoofdband", "hersenen")
            name.contains("Qtronix", ignoreCase = true) || name.contains("Libra 90", ignoreCase = true) ->
                listOf("qtronix", "libra", "90", "muis", "bal", "trackball", "ergonomisch", "usb", "invoer")
            else -> emptyList()
        }
    }

    private fun greetingReply(products: List<Product>): String {
        val hint = if (products.isEmpty()) {
            "Voeg producten toe om erover te vragen."
        } else {
            "Vraag bijvoorbeeld: «Wat is ${products.first().name}?» of tik: «Welke producten hebben jullie?»."
        }
        return buildString {
            appendLine("Hallo! Leuk dat je er bent.")
            appendLine()
            appendLine("Ik ben je productassistent. Ik werk op je telefoon — je hebt geen internet nodig voor mijn antwoorden.")
            appendLine()
            appendLine("Ik gebruik de teksten bij de producten in deze app. Ik leg het rustig en in korte stukjes uit.")
            appendLine()
            append(hint)
        }.trim()
    }

    private fun formatProductList(products: List<Product>): String {
        if (products.isEmpty()) {
            return "Er staan nog geen producten in de lijst."
        }
        return buildString {
            appendLine("Dit staat er nu in de app:")
            appendLine()
            for (p in products) {
                append("• ")
                append(p.name)
                if (p.description.isNotBlank()) {
                    appendLine()
                    append("  ")
                    appendLine(firstSentenceOrShort(p.description))
                } else {
                    appendLine()
                    appendLine("  (Nog geen korte uitleg — die kun je zelf toevoegen.)")
                }
                appendLine()
            }
        }.trimEnd()
    }

    private fun formatSingleProduct(p: Product): String {
        if (p.description.isBlank()) {
            return buildString {
                appendLine(p.name)
                appendLine()
                append(
                    "Voor dit product staat nog geen vaste uitleg in de app. " +
                        "Vraag je begeleider of vul zelf een duidelijke beschrijving in bij het product.",
                )
            }.trim()
        }
        return buildString {
            appendLine(p.name)
            appendLine()
            append(formatDescriptionForReading(p.description))
        }.trim()
    }

    private fun formatMultiAnswer(list: List<Product>): String {
        return buildString {
            appendLine("Ik vind meerdere producten die hierbij passen:")
            appendLine()
            for (p in list) {
                appendLine("— ${p.name}")
                appendLine(firstSentenceOrShort(p.description))
                appendLine()
            }
            append("Kies er één en stel gerust een vervolgvraag met de naam van het product.")
        }.trim()
    }

    private fun firstSentenceOrShort(desc: String): String {
        val one = desc.split('.').firstOrNull()?.trim()?.takeIf { it.length > 8 } ?: desc.trim()
        return if (one.length > 140) one.take(137).trimEnd() + "…" else one
    }

    private fun formatDescriptionForReading(description: String): String {
        val sentences = description.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (sentences.size <= 1) return description.trim()
        return sentences.joinToString("\n\n") { it }
    }

    private fun friendlyNoMatch(products: List<Product>): String {
        val examples = products.asSequence()
            .filter { it.description.isNotBlank() }
            .take(4)
            .map { it.name }
            .toList()
        val suffix = if (examples.isEmpty()) {
            "Probeer de naam van een product te typen, of vraag: «Welke producten hebben jullie?»."
        } else {
            "Probeer bijvoorbeeld: " + examples.joinToString(", ") + "."
        }
        return buildString {
            appendLine("Ik kan je vraag niet goed koppelen aan één product.")
            appendLine()
            appendLine(suffix)
        }.trim()
    }
}
