package com.middin.innovatie.desktop.ui;

/** Retro and default desktop themes (Instellingen → Thema). */
public enum DesktopThemeId {
    DEFAULT("default", "Default"),
    DOS("dos", "DOS"),
    WIN95("win95", "Windows 95"),
    WIN98("win98", "Windows 98"),
    WINXP("winxp", "Windows XP");

    private final String id;
    private final String label;

    DesktopThemeId(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    /** Drives shared button chrome (3D Windows vs CRT vs modern flat). */
    public ThemeUiFamily uiFamily() {
        return switch (this) {
            case WIN95, WIN98, WINXP -> ThemeUiFamily.WINDOWS_CLASSIC;
            case DOS -> ThemeUiFamily.CRT;
            default -> ThemeUiFamily.MODERN;
        };
    }

    public static DesktopThemeId fromId(String raw) {
        if (raw == null || raw.isBlank()) return DEFAULT;
        String t = raw.trim().toLowerCase();
        // Legacy light/dark/system prefs map to default.
        if ("system".equals(t) || "light".equals(t) || "dark".equals(t) || "terminal".equals(t) || "commodore".equals(t) || "ascii".equals(t)) return DEFAULT;
        for (DesktopThemeId theme : values()) {
            if (theme.id.equals(t)) return theme;
        }
        return DEFAULT;
    }
}
