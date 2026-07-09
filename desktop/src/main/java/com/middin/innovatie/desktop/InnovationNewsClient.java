package com.middin.innovatie.desktop;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Simplified RSS loader (same feeds as InnovationRssSources). */
public final class InnovationNewsClient {
    private static final String[][] FEEDS = {
        {"https://www.beckershospitalreview.com/feed/", "Becker's Hospital Review"},
        {"https://www.healthcarefinancenews.com/rss.xml", "Healthcare Finance News"},
        {"https://www.medicaldevice-network.com/feed/", "Medical Device Network"},
        {"https://www.fiercehealthcare.com/rss/xml", "Fierce Healthcare"},
    };
    private static final Pattern ITEM = Pattern.compile("<item[\\s>](.*?)</item>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE = Pattern.compile("<title>(?:<!\\[CDATA\\[)?(.*?)(?:\\]\\]>)?</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern LINK = Pattern.compile("<link>(?:<!\\[CDATA\\[)?(.*?)(?:\\]\\]>)?</link>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public static List<NewsItem> load() {
        List<NewsItem> all = new ArrayList<>();
        for (String[] feed : FEEDS) {
            try {
                all.addAll(fetch(feed[0], feed[1]));
            } catch (IOException ignored) {
            }
            if (all.size() >= 8) break;
        }
        if (all.isEmpty()) return fallback();
        return all.subList(0, Math.min(8, all.size()));
    }

    private static List<NewsItem> fetch(String url, String source) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setConnectTimeout(12_000);
        conn.setReadTimeout(12_000);
        conn.setRequestProperty("User-Agent", "MiddinInnovatie-Desktop/" + AppVersion.NAME);
        String xml = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        List<NewsItem> items = new ArrayList<>();
        Matcher m = ITEM.matcher(xml);
        while (m.find() && items.size() < 4) {
            String block = m.group(1);
            String title = match(TITLE, block);
            String link = match(LINK, block);
            if (title != null && !title.isBlank()) {
                items.add(new NewsItem(unescape(title.trim()), source, link == null ? "" : link.trim()));
            }
        }
        return items;
    }

    private static String match(Pattern p, String block) {
        Matcher m = p.matcher(block);
        return m.find() ? m.group(1) : null;
    }

    private static String unescape(String s) {
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
    }

    private static List<NewsItem> fallback() {
        return List.of(
            new NewsItem("Innovatie in zorg en ondersteuning — overzicht", "Middin Innovatie", ""),
            new NewsItem("Digitale hulpmiddelen en wearables in de zorg", "Middin Innovatie", ""),
            new NewsItem("AI en assistieve technologie voor cliënten", "Middin Innovatie", "")
        );
    }

    private InnovationNewsClient() {}
}
