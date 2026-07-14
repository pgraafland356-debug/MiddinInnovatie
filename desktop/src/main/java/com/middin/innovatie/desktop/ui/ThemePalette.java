package com.middin.innovatie.desktop.ui;

import java.awt.Color;
import java.awt.Font;

/** Color and font tokens for one desktop theme. */
final class ThemePalette {
    final Color primary;
    final Color primaryLight;
    final Color secondary;
    final Color text;
    final Color textLight;
    final Color background;
    final Color surfaceVariant;
    final Color border;
    final Color borderInput;
    final Color onPrimary;
    final Font fontTitle;
    final Font fontBody;
    final Font fontSmall;
    final Font fontNav;
    final boolean bevelButtons;

    ThemePalette(
        Color primary,
        Color primaryLight,
        Color secondary,
        Color text,
        Color textLight,
        Color background,
        Color surfaceVariant,
        Color border,
        Color borderInput,
        Color onPrimary,
        Font fontTitle,
        Font fontBody,
        Font fontSmall,
        Font fontNav,
        boolean bevelButtons
    ) {
        this.primary = primary;
        this.primaryLight = primaryLight;
        this.secondary = secondary;
        this.text = text;
        this.textLight = textLight;
        this.background = background;
        this.surfaceVariant = surfaceVariant;
        this.border = border;
        this.borderInput = borderInput;
        this.onPrimary = onPrimary;
        this.fontTitle = fontTitle;
        this.fontBody = fontBody;
        this.fontSmall = fontSmall;
        this.fontNav = fontNav;
        this.bevelButtons = bevelButtons;
    }

    static ThemePalette forTheme(DesktopThemeId theme) {
        return switch (theme) {
            case DEFAULT -> defaultTheme();
            case DOS -> dosTheme();
            case WIN95 -> win95Theme();
            case WIN98 -> win98Theme();
            case WINXP -> winXpTheme();
        };
    }

    private static ThemePalette defaultTheme() {
        Font sans = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        return new ThemePalette(
            c(0x00, 0x1A, 0x9E),
            c(0xF2, 0xF3, 0xFA),
            c(0xF9, 0xF2, 0xC8),
            c(0x00, 0x1A, 0x9E),
            c(0x77, 0x77, 0x77),
            Color.WHITE,
            c(0xF2, 0xF2, 0xF2),
            c(0xCC, 0xCC, 0xCC),
            c(0x99, 0x99, 0x99),
            Color.WHITE,
            sans.deriveFont(Font.BOLD, 22f),
            sans,
            sans.deriveFont(12f),
            sans.deriveFont(12f),
            false
        );
    }

    private static ThemePalette dosTheme() {
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        Color gray = c(0xAA, 0xAA, 0xAA);
        return new ThemePalette(
            Color.WHITE,
            c(0x1A, 0x1A, 0x1A),
            c(0x55, 0x55, 0x55),
            gray,
            c(0x66, 0x66, 0x66),
            Color.BLACK,
            c(0x11, 0x11, 0x11),
            c(0x55, 0x55, 0x55),
            c(0x88, 0x88, 0x88),
            Color.BLACK,
            mono.deriveFont(Font.BOLD, 20f),
            mono,
            mono.deriveFont(12f),
            mono.deriveFont(12f),
            false
        );
    }

    private static ThemePalette win95Theme() {
        Font sans = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        Color gray = c(0xC0, 0xC0, 0xC0);
        return new ThemePalette(
            c(0x00, 0x00, 0x80),
            gray,
            c(0xDF, 0xDF, 0xDF),
            Color.BLACK,
            c(0x44, 0x44, 0x44),
            gray,
            Color.WHITE,
            Color.BLACK,
            Color.BLACK,
            Color.WHITE,
            sans.deriveFont(Font.BOLD, 18f),
            sans,
            sans.deriveFont(12f),
            sans.deriveFont(12f),
            true
        );
    }

    private static ThemePalette win98Theme() {
        Font sans = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        Color gray = c(0xC0, 0xC0, 0xC0);
        return new ThemePalette(
            c(0x00, 0x00, 0xA0),
            gray,
            c(0xD4, 0xD0, 0xC8),
            Color.BLACK,
            c(0x33, 0x33, 0x33),
            c(0x3A, 0x6E, 0xA5),
            Color.WHITE,
            Color.BLACK,
            Color.BLACK,
            Color.WHITE,
            sans.deriveFont(Font.BOLD, 18f),
            sans,
            sans.deriveFont(12f),
            sans.deriveFont(12f),
            true
        );
    }

    private static ThemePalette winXpTheme() {
        Font sans = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        Color luna = c(0x24, 0x5E, 0xDC);
        return new ThemePalette(
            luna,
            c(0xD8, 0xE5, 0xF8),
            c(0xEC, 0xE9, 0xD8),
            Color.BLACK,
            c(0x55, 0x55, 0x55),
            c(0xEC, 0xE9, 0xD8),
            Color.WHITE,
            c(0x7A, 0x96, 0xDF),
            c(0x7A, 0x96, 0xDF),
            Color.WHITE,
            sans.deriveFont(Font.BOLD, 20f),
            sans,
            sans.deriveFont(12f),
            sans.deriveFont(12f),
            true
        );
    }

    private static Color c(int r, int g, int b) {
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
