package com.middin.innovatie.desktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Checks GitHub release manifest and installs full Windows setup EXE. */
public final class DesktopAppUpdater {

    public static final class WindowsRelease {
        public final int versionCode;
        public final String versionName;
        public final String setupUrl;
        public final String sha256;
        public final String changelog;

        public WindowsRelease(int versionCode, String versionName, String setupUrl, String sha256, String changelog) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.setupUrl = setupUrl;
            this.sha256 = sha256;
            this.changelog = changelog;
        }
    }

    public WindowsRelease fetchLatestRelease(String endpointUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(endpointUrl.trim()).toURL().openConnection();
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(15_000);
        conn.setRequestMethod("GET");
        try {
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code);
            }
            String body;
            try (InputStream in = conn.getInputStream()) {
                body = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            return parseWindowsRelease(body, AppVersion.CODE);
        } finally {
            conn.disconnect();
        }
    }

    static WindowsRelease parseWindowsRelease(String jsonBody, int currentVersionCode) throws IOException {
        String json = jsonBody.trim();
        int versionCode = extractInt(json, "versionCode");
        if (versionCode <= currentVersionCode) return null;

        String windowsBlock = extractObject(json, "windows");
        String setupUrl;
        String sha256;
        if (windowsBlock != null) {
            setupUrl = extractString(windowsBlock, "setupUrl");
            sha256 = extractString(windowsBlock, "sha256");
        } else {
            setupUrl = extractString(json, "setupUrl");
            sha256 = extractString(json, "sha256");
        }
        if (setupUrl == null || sha256 == null) {
            throw new IOException("Manifest mist windows.setupUrl of sha256");
        }
        return new WindowsRelease(
                versionCode,
                nullToEmpty(extractString(json, "versionName")),
                setupUrl,
                sha256,
                nullToEmpty(extractString(json, "changelog")));
    }

    public File downloadSetup(WindowsRelease release, File cacheDir) throws IOException {
        cacheDir.mkdirs();
        File out = new File(cacheDir, "MiddinInnovatie-Setup-" + release.versionCode + ".exe");
        HttpURLConnection conn = (HttpURLConnection) URI.create(release.setupUrl).toURL().openConnection();
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(300_000);
        try {
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("Download HTTP " + code);
            }
            try (InputStream in = conn.getInputStream(); FileOutputStream fos = new FileOutputStream(out)) {
                in.transferTo(fos);
            }
        } finally {
            conn.disconnect();
        }
        return out;
    }

    public boolean verifySha256(File file, String expectedSha) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IOException(e);
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().equalsIgnoreCase(expectedSha.trim());
    }

    public void launchInstaller(File setupExe) throws IOException {
        if (!setupExe.isFile()) {
            throw new IOException("Setup-bestand ontbreekt: " + setupExe);
        }
        new ProcessBuilder(setupExe.getAbsolutePath())
                .directory(setupExe.getParentFile())
                .start();
    }

    private static int extractInt(String json, String key) throws IOException {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(json);
        if (!m.find()) throw new IOException("Manifest mist " + key);
        return Integer.parseInt(m.group(1));
    }

    private static String extractString(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        if (!m.find()) return null;
        return unescapeJson(m.group(1));
    }

    private static String extractObject(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\{").matcher(json);
        if (!m.find()) return null;
        int start = m.end() - 1;
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return json.substring(start, i + 1);
            }
        }
        return null;
    }

    private static String unescapeJson(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
