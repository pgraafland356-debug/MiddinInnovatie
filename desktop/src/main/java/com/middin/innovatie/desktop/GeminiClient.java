package com.middin.innovatie.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GeminiClient {
    private static final String MODEL = "gemini-1.5-flash";
    private static final Pattern TEXT_FIELD = Pattern.compile("\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final int TIMEOUT_MS = 45_000;

    public static String generate(String apiKey, String prompt) throws IOException {
        String key = apiKey == null ? "" : apiKey.trim();
        if (key.isEmpty()) throw new IOException("Voeg je Gemini API-sleutel toe onder Meer → Instellingen.");
        String body = "{\"contents\":[{\"parts\":[{\"text\":" + jsonString(prompt) + "}]}]}";
        URI uri = URI.create(
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + key
        );
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(bytes);
        }
        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String response = readStream(stream);
        if (code < 200 || code >= 300) {
            throw new IOException("Gemini HTTP " + code + ": " + shorten(response, 300));
        }
        String text = extractFirstText(response);
        if (text == null || text.isBlank()) throw new IOException("Leeg antwoord van Gemini.");
        return text.trim();
    }

    private static String extractFirstText(String json) {
        Matcher m = TEXT_FIELD.matcher(json);
        return m.find() ? unescapeJson(m.group(1)) : null;
    }

    private static String jsonString(String value) {
        return "\"" + escapeJson(value == null ? "" : value) + "\"";
    }

    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                sb.append(c);
                continue;
            }
            char n = s.charAt(++i);
            switch (n) {
                case '\\', '"', '/' -> sb.append(n);
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                default -> sb.append(n);
            }
        }
        return sb.toString();
    }

    private static String readStream(InputStream in) throws IOException {
        if (in == null) return "";
        try (Scanner sc = new Scanner(in, StandardCharsets.UTF_8)) {
            sc.useDelimiter("\\A");
            return sc.hasNext() ? sc.next() : "";
        }
    }

    private static String shorten(String s, int max) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }

    private GeminiClient() {}
}
