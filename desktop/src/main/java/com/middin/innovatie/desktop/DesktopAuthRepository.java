package com.middin.innovatie.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** POST auth/login against the configured API (same contract as Android AuthRepository). */
public final class DesktopAuthRepository {
    private DesktopAuthRepository() {}

    public static void signIn(String baseUrl, String username, String password) throws IOException {
        String url = joinApiPath(baseUrl, "auth/login");
        String body = "{\"username\":\"" + escapeJson(username) + "\",\"password\":\"" + escapeJson(password) + "\"}";
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(15_000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        try {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            InputStream in = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            String response = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code + (response.isBlank() ? "" : ": " + response));
            }
            if (!hasBearerToken(response)) {
                throw new IOException("Login succeeded but response had no token.");
            }
        } finally {
            conn.disconnect();
        }
    }

    private static String joinApiPath(String baseUrl, String path) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (!path.startsWith("/")) path = "/" + path;
        return base + path;
    }

    private static boolean hasBearerToken(String json) {
        return extractJsonString(json, "token") != null
                || extractJsonString(json, "accessToken") != null
                || extractJsonString(json, "access_token") != null;
    }

    private static String extractJsonString(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
