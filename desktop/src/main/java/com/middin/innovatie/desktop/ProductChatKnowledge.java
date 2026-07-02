package com.middin.innovatie.desktop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Port of Android ProductChatKnowledge — same answers as the mobile app. */
public final class ProductChatKnowledge {
    public static final String AUTHOR_USER = "Jij";
    public static final String AUTHOR_BOT = "Productassistent";

    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-zA-Zà-ëìòùáéíóúüßœæ0-9]+");
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

    private static final Set<String> STOP = Set.of(
        "de", "het", "een", "en", "van", "voor", "met", "naar", "over", "wat", "wie", "hoe", "waar",
        "kan", "kun", "kunnen", "wil", "willen", "weten", "graag", "als", "maar", "ook", "nog", "niet",
        "die", "dat", "dit", "zijn", "is", "ben", "bij", "uit", "aan", "er", "te", "om", "of", "zo",
        "eens", "even", "mag", "mijn", "jouw", "hier", "daar", "waarom", "doet", "help", "helpt",
        "iets", "meer", "veel",
        "what", "which", "when", "where", "why", "how", "who", "the", "you", "your", "have", "has",
        "does", "did", "can", "could", "would", "should", "please", "tell", "about", "me", "any"
    );

    private ProductChatKnowledge() {}

    public static String welcomeMessage() {
        return String.join("\n",
            "Hallo! Ik ben je productassistent.",
            "",
            "Ik werk op je telefoon: je hebt geen internet nodig om mij te gebruiken.",
            "",
            "Stel een vraag over de hulpmiddelen in deze app. Bijvoorbeeld:",
            "• «Wat doet de Somnox?»",
            "• «Welke producten zijn er?»",
            "• «Iets voor beter slapen?»",
            "",
            "Ik antwoord met eenvoudige taal op basis van de productinformatie hier in de app."
        );
    }

    public static String answer(String question, List<Catalog.Product> products) {
        String q = question == null ? "" : question.trim();
        if (q.isEmpty()) return friendlyNoMatch(products);

        String lower = normalize(q);
        if (isWhoAreYouQuestion(lower)) return whoAreYouReply(products);
        if (isHowCanYouHelpQuestion(lower)) return howCanYouHelpReply(products);
        if (isGreeting(lower)) return greetingReply(products);
        if (wantsProductList(lower)) return formatProductList(products);

        List<String> qTokens = tokenize(q);
        if (products.isEmpty()) {
            return "Er staan nog geen producten in de app. Voeg eerst producten toe op het tabblad Producten.";
        }

        List<ScoredProduct> scored = new ArrayList<>();
        for (Catalog.Product p : products) {
            scored.add(new ScoredProduct(p, scoreProduct(qTokens, lower, p)));
        }
        scored.sort(Comparator.comparingInt((ScoredProduct s) -> s.score).reversed());
        ScoredProduct best = scored.get(0);
        int thresholdStrong = 4;
        int thresholdWeak = 1;

        List<ScoredProduct> strong = scored.stream()
            .filter(s -> s.score >= thresholdStrong)
            .collect(Collectors.toList());
        if (strong.size() >= 2
            && strong.get(0).score == strong.get(1).score
            && strong.get(0).score >= thresholdStrong) {
            return formatMultiAnswer(strong.stream().limit(3).map(s -> s.product).collect(Collectors.toList()));
        }
        if (best.score >= thresholdStrong) return formatSingleProduct(best.product);
        if (best.score >= thresholdWeak) {
            return formatSingleProduct(best.product)
                + "\n\n(Als dit niet klopt, typ dan de naam van het product zoals in de lijst.)";
        }
        return friendlyNoMatch(products);
    }

    private static String normalize(String s) {
        return s.toLowerCase(NL).replace('’', '\'').replaceAll("\\s+", " ").trim();
    }

    private static boolean isWhoAreYouQuestion(String lower) {
        if (lower.contains("wie ben jij") || lower.contains("wie ben je")) return true;
        if (lower.contains("wie zij jij") || lower.contains("wie zij je")) return true;
        if (lower.contains("wie je bent") || lower.contains("wie jij bent")) return true;
        if ((lower.contains("vertel") || lower.contains("vertellen") || lower.contains("zeggen") || lower.contains("weten"))
            && lower.contains("wie")
            && (lower.contains("je bent") || lower.contains("jij bent") || lower.contains("ben je") || lower.contains("ben jij"))) {
            return true;
        }
        return lower.contains("who are you") || lower.contains("tell me who you are");
    }

    private static boolean isHowCanYouHelpQuestion(String lower) {
        if (lower.contains("waarmee kan je mij helpen")) return true;
        if (lower.contains("waarmee kun je mij helpen")) return true;
        if (lower.contains("waarmee kan je me helpen")) return true;
        if (lower.contains("waarmee kun je me helpen")) return true;
        if (lower.contains("waarmee help je mij") || lower.contains("waarmee help je me")) return true;
        if (lower.contains("hoe kan je mij helpen") || lower.contains("hoe kun je mij helpen")) return true;
        if (lower.contains("hoe kan je me helpen") || lower.contains("hoe kun je me helpen")) return true;
        if (lower.contains("met wat kan je mij helpen") || lower.contains("met wat kun je mij helpen")) return true;
        return lower.contains("what can you help") && (lower.contains("me") || lower.contains("with"));
    }

    private static String whoAreYouReply(List<Catalog.Product> products) {
        String hint = products.isEmpty()
            ? "Als er producten in de app staan, kan ik je daar uitleg over geven. Voeg ze toe via het tabblad Producten."
            : "Vraag gerust over een product uit de lijst, of tik: «Welke producten hebben jullie?».";
        return String.join("\n",
            "Ik ben de Productassistent van deze Middin Innovatie-app.",
            "",
            "Ik ben geen mens, maar een hulp in de app. Ik gebruik de teksten bij de producten "
                + "die hier staan en probeer je vragen daarop te beantwoorden — in rustig, "
                + "begrijpelijk Nederlands en zonder internet.",
            "",
            hint
        );
    }

    private static String howCanYouHelpReply(List<Catalog.Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ik kan je helpen met vragen over de producten in deze app.\n\n");
        sb.append("Zo kun je me gebruiken:\n");
        sb.append("• Uitleg over wat een product doet of waarvoor het bedoeld is\n");
        sb.append("• De informatie in makkelijke stukjes en heldere taal\n");
        sb.append("• Een overzicht als je vraagt welke producten er zijn\n\n");
        sb.append("Ik gebruik alleen wat er bij elk product in de app staat. "
            + "Voor medisch advies, vergoedingen of jouw persoonlijke situatie moet je "
            + "altijd naar je begeleider, arts of de leverancier van het hulpmiddel.");
        if (products.isEmpty()) {
            sb.append("\n\nEr staan nog geen producten in de app. Voeg eerst producten toe; "
                + "daarna kan ik je er meer over vertellen.");
        }
        sb.append("\n\nTyp gerust de naam van een product of stel een concrete vraag.");
        return sb.toString();
    }

    private static boolean isGreeting(String lower) {
        List<String> greetings = List.of(
            "hallo", "hoi", "hey", "hi", "goedemorgen", "goedemiddag", "goedenavond", "dag",
            "hello", "thanks", "bedankt", "dank"
        );
        if (lower.length() <= 12 && greetings.stream().anyMatch(g -> lower.equals(g) || lower.startsWith(g + " "))) {
            return true;
        }
        return greetings.stream().anyMatch(lower::startsWith) && lower.length() < 40;
    }

    private static boolean wantsProductList(String lower) {
        if (lower.contains("lijst") || lower.contains("overzicht")) return true;
        if (lower.contains("alle") && lower.contains("product")) return true;
        if (lower.contains("welke") && lower.contains("product")) return true;
        if (lower.contains("wat heb") || lower.contains("wat zijn")) return true;
        if (lower.contains("which") && lower.contains("product")) return true;
        if (lower.contains("list") && lower.contains("product")) return true;
        if (lower.contains("all") && lower.contains("product")) return true;
        return lower.contains("what") && lower.contains("product");
    }

    private static List<String> tokenize(String s) {
        List<String> tokens = new ArrayList<>();
        Matcher m = TOKEN_PATTERN.matcher(s.toLowerCase(NL));
        while (m.find()) {
            String t = m.group();
            if (t.length() >= 2 && !STOP.contains(t)) tokens.add(t);
        }
        return tokens;
    }

    private static int scoreProduct(List<String> qTokens, String qLower, Catalog.Product p) {
        String nameLower = p.name().toLowerCase(NL);
        String descLower = p.description().toLowerCase(NL);
        int score = 0;
        if (qLower.length() >= 3 && nameLower.contains(qLower)) score += 25;
        for (String t : qTokens) {
            if (t.length() >= 3 && nameLower.contains(t)) score += 10;
            if (t.length() >= 4 && descLower.contains(t)) score += 3;
            for (String part : nameLower.split("\\W+")) {
                if (part.length() >= 3 && (part.equals(t) || part.startsWith(t) || t.startsWith(part))) score += 6;
            }
        }
        for (String syn : productSynonyms(p.name())) {
            if (qLower.contains(syn)) score += 12;
        }
        return score;
    }

    private static List<String> productSynonyms(String name) {
        String n = name;
        if (containsIgnoreCase(n, "Somnox")) return List.of("somnox", "slaaprobot", "slaap", "slapen", "inslapen");
        if (containsIgnoreCase(n, "Motionwatch")) return List.of("motionwatch", "actigraaf", "pols", "slaapritme");
        if (containsIgnoreCase(n, "Tessa")) return List.of("tessa", "tinybot", "robot");
        if (containsIgnoreCase(n, "Luvion") || containsIgnoreCase(n, "White noise")) {
            return List.of("luvion", "witte", "ruis", "white", "noise", "geluid", "slaap", "slapen");
        }
        if (containsIgnoreCase(n, "Mowoot")) return List.of("mowoot", "buik", "obstipatie", "darm");
        if (containsIgnoreCase(n, "Mijn eigen plan")) return List.of("mijneigenplan", "eigen", "plan");
        if (containsIgnoreCase(n, "Visitaal")) {
            return List.of("visitaal", "chat", "pictogrammen", "whatsapp", "bericht", "praten", "communicatie", "voorlezen");
        }
        if (containsIgnoreCase(n, "BIG Launcher")) {
            return List.of("big", "launcher", "grote", "letters", "iconen", "start", "android", "senior", "sos");
        }
        if (containsIgnoreCase(n, "launcher")) return List.of("launcher", "grote", "iconen", "start");
        if (containsIgnoreCase(n, "VR") || n.toLowerCase(NL).contains("vr-")) {
            return List.of("vr", "virtual", "bril");
        }
        if (containsIgnoreCase(n, "CRDL")) return List.of("crdl", "muziek", "aanraking");
        if (containsIgnoreCase(n, "Orcam")) return List.of("orcam", "voorlezen", "blind", "slechtziend");
        if (containsIgnoreCase(n, "handsteady")) return List.of("handsteady", "beker", "tremor", "trillen");
        if (containsIgnoreCase(n, "Braintrainer")) return List.of("braintrainer", "cognitief", "hersenen", "training");
        if (containsIgnoreCase(n, "Onpoint") || containsIgnoreCase(n, "tril stylus")) {
            return List.of("onpoint", "stylus", "joystick", "muis");
        }
        if (containsIgnoreCase(n, "Muse")) return List.of("muse", "eeg", "meditatie", "hoofdband", "hersenen");
        if (containsIgnoreCase(n, "Qtronix") || containsIgnoreCase(n, "Libra 90")) {
            return List.of("qtronix", "libra", "90", "muis", "bal", "trackball", "ergonomisch", "usb", "invoer");
        }
        return List.of();
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase(NL).contains(needle.toLowerCase(NL));
    }

    private static String greetingReply(List<Catalog.Product> products) {
        String hint = products.isEmpty()
            ? "Voeg producten toe om erover te vragen."
            : "Vraag bijvoorbeeld: «Wat is " + products.get(0).name() + "?» of tik: «Welke producten hebben jullie?».";
        return String.join("\n",
            "Hallo! Leuk dat je er bent.",
            "",
            "Ik ben je productassistent. Ik werk op je telefoon — je hebt geen internet nodig voor mijn antwoorden.",
            "",
            "Ik gebruik de teksten bij de producten in deze app. Ik leg het rustig en in korte stukjes uit.",
            "",
            hint
        );
    }

    private static String formatProductList(List<Catalog.Product> products) {
        if (products.isEmpty()) return "Er staan nog geen producten in de lijst.";
        StringBuilder sb = new StringBuilder("Dit staat er nu in de app:\n\n");
        for (Catalog.Product p : products) {
            sb.append("• ").append(p.name());
            if (!p.description().isBlank()) {
                sb.append('\n').append("  ").append(firstSentenceOrShort(p.description())).append('\n');
            } else {
                sb.append('\n').append("  (Nog geen korte uitleg — die kun je zelf toevoegen.)\n");
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    private static String formatSingleProduct(Catalog.Product p) {
        if (p.description().isBlank()) {
            return p.name() + "\n\nVoor dit product staat nog geen vaste uitleg in de app. "
                + "Vraag je begeleider of vul zelf een duidelijke beschrijving in bij het product.";
        }
        return p.name() + "\n\n" + formatDescriptionForReading(p.description());
    }

    private static String formatMultiAnswer(List<Catalog.Product> list) {
        StringBuilder sb = new StringBuilder("Ik vind meerdere producten die hierbij passen:\n\n");
        for (Catalog.Product p : list) {
            sb.append("— ").append(p.name()).append('\n');
            sb.append(firstSentenceOrShort(p.description())).append("\n\n");
        }
        sb.append("Kies er één en stel gerust een vervolgvraag met de naam van het product.");
        return sb.toString().trim();
    }

    private static String firstSentenceOrShort(String desc) {
        String[] parts = desc.split("\\.");
        String one = parts.length > 0 && parts[0].trim().length() > 8 ? parts[0].trim() : desc.trim();
        return one.length() > 140 ? one.substring(0, 137).trim() + "…" : one;
    }

    private static String formatDescriptionForReading(String description) {
        String[] sentences = SENTENCE_SPLIT.split(description.trim());
        List<String> cleaned = new ArrayList<>();
        for (String s : sentences) {
            String t = s.trim();
            if (!t.isEmpty()) cleaned.add(t);
        }
        if (cleaned.size() <= 1) return description.trim();
        return String.join("\n\n", cleaned);
    }

    private static String friendlyNoMatch(List<Catalog.Product> products) {
        List<String> examples = products.stream()
            .filter(p -> !p.description().isBlank())
            .limit(4)
            .map(Catalog.Product::name)
            .collect(Collectors.toList());
        String suffix = examples.isEmpty()
            ? "Probeer de naam van een product te typen, of vraag: «Welke producten hebben jullie?»."
            : "Probeer bijvoorbeeld: " + String.join(", ", examples) + ".";
        return "Ik kan je vraag niet goed koppelen aan één product.\n\n" + suffix;
    }

    private record ScoredProduct(Catalog.Product product, int score) {}
}
