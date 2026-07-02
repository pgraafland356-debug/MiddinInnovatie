package com.middin.innovatie.desktop.ui;

/** Loads and applies the active desktop theme. */
public final class ThemeManager {
    private static DesktopThemeId current = DesktopThemeId.DEFAULT;

    private ThemeManager() {}

    public static DesktopThemeId current() {
        return current;
    }

    public static void apply(String themeId) {
        apply(DesktopThemeId.fromId(themeId));
    }

    public static void apply(DesktopThemeId theme) {
        current = theme;
        MiddinTheme.loadPalette(ThemePalette.forTheme(theme), theme);
    }
}
