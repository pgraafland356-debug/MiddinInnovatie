package com.middin.innovatie.desktop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public final class DesktopPreferences {
    public static final String ENDPOINT_SETTINGS_USERNAME = "pieter-bas";
    private static final String DEFAULT_API = "http://10.0.2.2:8080";

    private final File stateFile;
    private Properties props;

    public DesktopPreferences(File stateFile) {
        this.stateFile = stateFile;
        reload();
    }

    public void reload() {
        props = new Properties();
        if (stateFile.exists()) {
            try (var in = Files.newInputStream(stateFile.toPath())) {
                props.load(in);
            } catch (IOException ignored) {
            }
        }
    }

    public void save() throws IOException {
        stateFile.getParentFile().mkdirs();
        try (var out = Files.newOutputStream(stateFile.toPath())) {
            props.store(out, "Middin Innovatie desktop");
        }
    }

    public boolean isLoggedIn() {
        return "true".equals(props.getProperty("logged_in"));
    }

    public void setLoggedIn(boolean value) {
        props.setProperty("logged_in", value ? "true" : "false");
    }

    public String getUsername() {
        return props.getProperty("username", "");
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank()) props.remove("username");
        else props.setProperty("username", username.trim());
    }

    public boolean canConfigureEndpoints() {
        return getUsername().equalsIgnoreCase(ENDPOINT_SETTINGS_USERNAME);
    }

    public String getTheme() {
        return props.getProperty("theme", "default");
    }

    public void setTheme(String theme) {
        props.setProperty("theme", theme == null ? "default" : theme);
    }

    public String getLocale() {
        return props.getProperty("locale", "nl");
    }

    public void setLocale(String locale) {
        props.setProperty("locale", locale == null ? "nl" : locale);
    }

    public String getGeminiApiKey() {
        return props.getProperty("gemini_api_key", "").trim();
    }

    public void setGeminiApiKey(String key) {
        String t = key == null ? "" : key.trim();
        if (t.isEmpty()) props.remove("gemini_api_key");
        else props.setProperty("gemini_api_key", t);
    }

    public String getApiBaseUrlOverride() {
        return props.getProperty("api_base_url", "").trim();
    }

    public void setApiBaseUrlOverride(String url) {
        String t = url == null ? "" : url.trim();
        if (t.isEmpty()) props.remove("api_base_url");
        else props.setProperty("api_base_url", t);
    }

    public String getEffectiveApiBaseUrl() {
        String o = getApiBaseUrlOverride();
        return o.isEmpty() ? DEFAULT_API : o;
    }

    public String getUpdateFeedOverride() {
        return props.getProperty("update_feed_url", "").trim();
    }

    public void setUpdateFeedOverride(String url) {
        String t = url == null ? "" : url.trim();
        if (t.isEmpty()) props.remove("update_feed_url");
        else props.setProperty("update_feed_url", t);
    }

    public String getEffectiveUpdateFeedUrl() {
        String o = getUpdateFeedOverride();
        return o.isEmpty() ? AppVersion.UPDATE_FEED_DEFAULT : o;
    }

    public int getUpdateNoticeDismissedCode() {
        try {
            return Integer.parseInt(props.getProperty("update_notice_dismissed_code", "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setUpdateNoticeDismissedCode(int code) {
        props.setProperty("update_notice_dismissed_code", Integer.toString(code));
    }
}
