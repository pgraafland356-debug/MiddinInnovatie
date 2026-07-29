package com.middin.innovatie.desktop;

import com.middin.innovatie.desktop.DesktopPreferences;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads releases/changelog.json from bundled resources and optional remote feed. */
public final class ChangelogLoader {

    public static final class Entry {
        public final int versionCode;
        public final String versionName;
        public final String dateIso;
        public final List<String> bullets;

        public Entry(int versionCode, String versionName, String dateIso, List<String> bullets) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.dateIso = dateIso;
            this.bullets = bullets;
        }

        public String displayLabel() {
            if (versionName != null && !versionName.isBlank()) {
                return versionName + " (" + versionCode + ")";
            }
            return String.valueOf(versionCode);
        }
    }

    private static final Pattern ENTRY_BLOCK = Pattern.compile(
            "\\{\\s*\"versionCode\"\\s*:\\s*(\\d+)\\s*,\\s*\"versionName\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"dateIso\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"bullets\"\\s*:\\s*\\[(.*?)\\]\\s*\\}",
            Pattern.DOTALL);
    private static final Pattern BULLET = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");

    private ChangelogLoader() {}

    public static String changelogUrlFromUpdateFeed(String updateFeedUrl) {
        if (updateFeedUrl == null) return "";
        String trimmed = updateFeedUrl.trim();
        if (trimmed.isEmpty()) return "";
        String marker = "latest.json";
        int idx = trimmed.toLowerCase().lastIndexOf(marker.toLowerCase());
        if (idx >= 0) {
            return trimmed.substring(0, idx) + "changelog.json";
        }
        return trimmed.replaceAll("/+$", "") + "/changelog.json";
    }

    public static List<Entry> load(DesktopPreferences prefs) {
        List<Entry> bundled = loadBundled();
        List<Entry> remote = fetchRemote(prefs.getEffectiveUpdateFeedUrl());
        return merge(remote, bundled);
    }

    static List<Entry> merge(List<Entry> remote, List<Entry> bundled) {
        Map<Integer, Entry> byCode = new LinkedHashMap<>();
        for (Entry e : bundled) {
            byCode.put(e.versionCode, e);
        }
        for (Entry e : remote) {
            byCode.put(e.versionCode, e);
        }
        List<Entry> out = new ArrayList<>(byCode.values());
        out.sort((a, b) -> Integer.compare(b.versionCode, a.versionCode));
        return out;
    }

    private static List<Entry> loadBundled() {
        try (InputStream in = ChangelogLoader.class.getResourceAsStream("/changelog.json")) {
            if (in == null) return List.of();
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return parse(body);
        } catch (IOException e) {
            return List.of();
        }
    }

    private static List<Entry> fetchRemote(String updateFeedUrl) {
        String url = changelogUrlFromUpdateFeed(updateFeedUrl);
        if (url.isEmpty()) return List.of();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) return List.of();
            String body;
            try (InputStream in = conn.getInputStream()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (!body.isEmpty() && body.charAt(0) == '\uFEFF') {
                body = body.substring(1);
            }
            return parse(body);
        } catch (IOException e) {
            return List.of();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    static List<Entry> parse(String jsonBody) {
        String json = jsonBody == null ? "" : jsonBody.trim();
        List<Entry> entries = new ArrayList<>();
        Matcher m = ENTRY_BLOCK.matcher(json);
        while (m.find()) {
            int versionCode = Integer.parseInt(m.group(1));
            String versionName = m.group(2);
            String dateIso = m.group(3);
            List<String> bullets = parseBullets(m.group(4));
            entries.add(new Entry(versionCode, versionName, dateIso, bullets));
        }
        return entries;
    }

    private static List<String> parseBullets(String arrayBody) {
        List<String> bullets = new ArrayList<>();
        Matcher m = BULLET.matcher(arrayBody);
        while (m.find()) {
            String raw = m.group(1);
            bullets.add(raw.replace("\\\"", "\"").replace("\\\\", "\\").trim());
        }
        return bullets;
    }

    public static int newestVersionCode(List<Entry> entries) {
        int max = 0;
        for (Entry e : entries) {
            if (e.versionCode > max) max = e.versionCode;
        }
        return max;
    }
}
