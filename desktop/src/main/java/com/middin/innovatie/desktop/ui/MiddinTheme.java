package com.middin.innovatie.desktop.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Middin Innovatie UI tokens — colors/fonts switch per [ThemeManager] theme. */
public final class MiddinTheme {
    public static Color PRIMARY = new Color(0x00, 0x1A, 0x9E);
    public static Color PRIMARY_LIGHT = new Color(0xF2, 0xF3, 0xFA);
    public static Color SECONDARY = new Color(0xF9, 0xF2, 0xC8);
    public static Color SECONDARY_DARK = new Color(0xF2, 0xE4, 0x91);
    public static Color GREEN = new Color(0x76, 0xE1, 0xCA);
    public static Color TEXT = PRIMARY;
    public static Color TEXT_LIGHT = new Color(0x77, 0x77, 0x77);
    public static Color BACKGROUND = Color.WHITE;
    public static Color SURFACE_VARIANT = new Color(0xF2, 0xF2, 0xF2);
    public static Color BORDER = new Color(0xCC, 0xCC, 0xCC);
    public static Color BORDER_INPUT = new Color(0x99, 0x99, 0x99);
    public static Color ON_PRIMARY = Color.WHITE;

    public static Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static Font FONT_SUBTITLE = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static Font FONT_BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static Font FONT_NAV = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    private static boolean bevelButtons;
    private static ThemeUiFamily uiFamily = ThemeUiFamily.MODERN;

    private MiddinTheme() {}

    static void loadPalette(ThemePalette p, DesktopThemeId theme) {
        PRIMARY = p.primary;
        PRIMARY_LIGHT = p.primaryLight;
        SECONDARY = p.secondary;
        TEXT = p.text;
        TEXT_LIGHT = p.textLight;
        BACKGROUND = p.background;
        SURFACE_VARIANT = p.surfaceVariant;
        BORDER = p.border;
        BORDER_INPUT = p.borderInput;
        ON_PRIMARY = p.onPrimary;
        FONT_TITLE = p.fontTitle;
        FONT_BODY = p.fontBody;
        FONT_SMALL = p.fontSmall;
        FONT_NAV = p.fontNav;
        bevelButtons = p.bevelButtons;
        uiFamily = theme.uiFamily();
        applyGlobal();
    }

    public static void applyGlobal() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("TextField.background", inputBackground());
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("PasswordField.background", inputBackground());
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", TEXT);
        UIManager.put("TextArea.background", inputBackground());
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("List.background", inputBackground());
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("RadioButton.background", BACKGROUND);
        UIManager.put("RadioButton.foreground", TEXT);
    }

    public static Border cardBorder() {
        if (uiFamily == ThemeUiFamily.ASCII) {
            return new CompoundBorder(
                new AsciiBoxBorder(BORDER, FONT_SMALL),
                new EmptyBorder(8, 10, 8, 10)
            );
        }
        if (bevelButtons) {
            return new CompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, BORDER),
                new EmptyBorder(12, 12, 12, 12)
            );
        }
        return new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(switch (uiFamily) {
            case WINDOWS_CLASSIC -> Color.WHITE;
            case ASCII -> SURFACE_VARIANT;
            default -> bevelButtons ? Color.WHITE : PRIMARY_LIGHT;
        });
        panel.setBorder(cardBorder());
        return panel;
    }

    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel bodyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    public static JTextField outlinedField(int columns) {
        JTextField f = new JTextField(columns);
        styleTextInput(f);
        return f;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        styleTextInput(f);
        return f;
    }

    private static void styleTextInput(javax.swing.JComponent field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT);
        field.setBackground(inputBackground());
        field.setBorder(inputBorder());
    }

    private static Color inputBackground() {
        return switch (uiFamily) {
            case WINDOWS_CLASSIC -> Color.WHITE;
            case CRT, ASCII -> SURFACE_VARIANT;
            default -> BACKGROUND;
        };
    }

    private static Border inputBorder() {
        if (uiFamily == ThemeUiFamily.ASCII) {
            return new CompoundBorder(
                new AsciiBoxBorder(BORDER_INPUT, FONT_SMALL),
                new EmptyBorder(6, 8, 6, 8)
            );
        }
        if (bevelButtons) {
            return BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        }
        return BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_INPUT, 1, uiFamily != ThemeUiFamily.CRT),
            new EmptyBorder(10, 12, 10, 12)
        );
    }

    public static JTextArea bodyArea() {
        JTextArea a = new JTextArea();
        a.setFont(FONT_BODY);
        a.setForeground(TEXT);
        a.setBackground(inputBackground());
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(8, 8, 8, 8));
        return a;
    }

    public static JScrollPane scroll(JTextArea area) {
        JScrollPane sp = new JScrollPane(area);
        if (uiFamily == ThemeUiFamily.ASCII) {
            sp.setBorder(new AsciiBoxBorder(BORDER, FONT_SMALL));
        } else if (bevelButtons) {
            sp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        } else {
            sp.setBorder(new LineBorder(BORDER, 1, true));
        }
        sp.getViewport().setBackground(switch (uiFamily) {
            case WINDOWS_CLASSIC -> Color.WHITE;
            case ASCII -> SURFACE_VARIANT;
            default -> bevelButtons ? Color.WHITE : BACKGROUND;
        });
        return sp;
    }

    public static JButton primaryButton(String text) {
        return styleActionButton(new JButton(text), true, false);
    }

    public static JButton textButton(String text) {
        return styleActionButton(new JButton(text), false, false);
    }

    public static JButton navButton(String label, boolean selected) {
        JButton b = new JButton(label);
        b.setFont(FONT_NAV);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(100, 56));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyNavStyle(b, selected);
        return b;
    }

    public static JButton menuRowButton(String title) {
        JButton row = new JButton(title);
        row.setFont(FONT_BODY.deriveFont(Font.BOLD));
        row.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        row.setFocusPainted(false);
        row.setOpaque(true);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        row.setAlignmentX(0f);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyMenuRowStyle(row);
        return row;
    }

    public static JButton themePickButton(DesktopThemeId theme, boolean selected) {
        ThemePalette p = ThemePalette.forTheme(theme);
        JButton b = new JButton(theme.label());
        b.setFont(p.fontSmall);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.setAlignmentX(0f);
        applyThemePreviewStyle(b, p, theme.uiFamily(), selected);
        return b;
    }

    private static JButton styleActionButton(JButton b, boolean primary, boolean selected) {
        b.setFont(primary ? FONT_BODY.deriveFont(Font.BOLD) : FONT_BODY);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switch (uiFamily) {
            case WINDOWS_CLASSIC -> applyWinClassicActionStyle(b, primary, selected);
            case CRT -> applyCrtActionStyle(b, primary, selected);
            case ASCII -> applyAsciiActionStyle(b, primary, selected);
            default -> applyModernActionStyle(b, primary, selected);
        }
        return b;
    }

    private static void applyModernActionStyle(JButton b, boolean primary, boolean selected) {
        if (primary) {
            b.setForeground(ON_PRIMARY);
            b.setBackground(PRIMARY);
            b.setBorderPainted(false);
            b.setBorder(new EmptyBorder(12, 24, 12, 24));
        } else {
            b.setForeground(PRIMARY);
            b.setBackground(BACKGROUND);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setBorder(new EmptyBorder(6, 8, 6, 8));
        }
    }

    private static void applyWinClassicActionStyle(JButton b, boolean primary, boolean selected) {
        b.setForeground(Color.BLACK);
        b.setBackground(PRIMARY_LIGHT);
        b.setBorderPainted(true);
        b.setContentAreaFilled(true);
        int bevel = (primary && selected) ? BevelBorder.LOWERED : BevelBorder.RAISED;
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createBevelBorder(bevel, Color.WHITE, BORDER),
            new EmptyBorder(6, 14, 6, 14)
        ));
    }

    private static void applyCrtActionStyle(JButton b, boolean primary, boolean selected) {
        b.setForeground(TEXT);
        if (primary) {
            b.setBackground(selected ? SURFACE_VARIANT : PRIMARY);
            b.setForeground(primary && !selected ? ON_PRIMARY : TEXT);
        } else {
            b.setBackground(BACKGROUND);
        }
        b.setBorderPainted(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, false),
            new EmptyBorder(8, 14, 8, 14)
        ));
    }

    private static void applyAsciiActionStyle(JButton b, boolean primary, boolean selected) {
        b.setForeground(primary ? ON_PRIMARY : TEXT);
        b.setBackground(primary ? PRIMARY : BACKGROUND);
        b.setBorderPainted(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            new AsciiBoxBorder(primary ? ON_PRIMARY : BORDER, FONT_SMALL),
            new EmptyBorder(6, 12, 6, 12)
        ));
        if (primary) {
            b.setFont(FONT_BODY.deriveFont(Font.BOLD));
        }
    }

    private static void applyNavStyle(JButton b, boolean selected) {
        switch (uiFamily) {
            case WINDOWS_CLASSIC -> {
                b.setBorderPainted(true);
                b.setBorder(BorderFactory.createBevelBorder(
                    selected ? BevelBorder.LOWERED : BevelBorder.RAISED,
                    Color.WHITE,
                    BORDER
                ));
                b.setBackground(PRIMARY_LIGHT);
                b.setForeground(TEXT);
                if (selected) b.setFont(FONT_NAV.deriveFont(Font.BOLD));
            }
            case CRT -> {
                b.setBorderPainted(true);
                b.setBorder(new LineBorder(selected ? TEXT : BORDER, selected ? 2 : 1, false));
                b.setBackground(selected ? SURFACE_VARIANT : BACKGROUND);
                b.setForeground(TEXT);
                if (selected) b.setFont(FONT_NAV.deriveFont(Font.BOLD));
            }
            case ASCII -> {
                b.setBorderPainted(true);
                b.setBorder(BorderFactory.createCompoundBorder(
                    new AsciiBoxBorder(selected ? TEXT : BORDER, FONT_SMALL),
                    new EmptyBorder(8, 10, 8, 10)
                ));
                b.setBackground(selected ? PRIMARY_LIGHT : BACKGROUND);
                b.setForeground(selected ? PRIMARY : TEXT_LIGHT);
                if (selected) b.setFont(FONT_NAV.deriveFont(Font.BOLD));
            }
            default -> {
                if (selected) {
                    b.setBorderPainted(false);
                    b.setBackground(PRIMARY_LIGHT);
                    b.setForeground(PRIMARY);
                    b.setFont(FONT_NAV.deriveFont(Font.BOLD));
                } else {
                    b.setBorderPainted(false);
                    b.setBackground(BACKGROUND);
                    b.setForeground(TEXT_LIGHT);
                }
            }
        }
    }

    private static void applyMenuRowStyle(JButton row) {
        switch (uiFamily) {
            case WINDOWS_CLASSIC -> {
                row.setForeground(Color.BLACK);
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, BORDER),
                    new EmptyBorder(12, 16, 12, 16)
                ));
            }
            case CRT -> {
                row.setForeground(TEXT);
                row.setBackground(SURFACE_VARIANT);
                row.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, false),
                    new EmptyBorder(12, 16, 12, 16)
                ));
            }
            case ASCII -> {
                row.setForeground(TEXT);
                row.setBackground(SURFACE_VARIANT);
                row.setBorder(BorderFactory.createCompoundBorder(
                    new AsciiBoxBorder(BORDER, FONT_SMALL),
                    new EmptyBorder(10, 14, 10, 14)
                ));
            }
            default -> {
                row.setForeground(TEXT);
                row.setBackground(PRIMARY_LIGHT);
                row.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(14, 16, 14, 16)
                ));
            }
        }
    }

    private static void applyThemePreviewStyle(
        JButton b,
        ThemePalette p,
        ThemeUiFamily family,
        boolean selected
    ) {
        switch (family) {
            case WINDOWS_CLASSIC -> {
                b.setForeground(Color.BLACK);
                b.setBackground(Color.WHITE);
                b.setBorderPainted(true);
                b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createBevelBorder(
                        selected ? BevelBorder.LOWERED : BevelBorder.RAISED,
                        Color.WHITE,
                        p.border
                    ),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
            case CRT -> {
                b.setForeground(p.text);
                b.setBackground(selected ? p.surfaceVariant : p.background);
                b.setBorderPainted(true);
                b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(selected ? p.text : p.border, selected ? 2 : 1, false),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
            case ASCII -> {
                b.setForeground(selected ? p.onPrimary : p.text);
                b.setBackground(selected ? p.primary : p.surfaceVariant);
                b.setBorderPainted(true);
                b.setBorder(BorderFactory.createCompoundBorder(
                    new AsciiBoxBorder(selected ? p.onPrimary : p.border, p.fontSmall),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
            default -> {
                b.setForeground(selected ? p.onPrimary : p.text);
                b.setBackground(selected ? p.primary : p.primaryLight);
                b.setBorderPainted(true);
                b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(selected ? p.primary : p.border, selected ? 2 : 1, true),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
        }
    }

    /** Re-apply nav chrome after selection changes (same theme). */
    public static void updateNavButton(JButton b, boolean selected) {
        applyNavStyle(b, selected);
    }

    public static JPanel contentPad(JPanel inner) {
        JPanel wrap = new JPanel(new java.awt.BorderLayout());
        wrap.setBackground(BACKGROUND);
        wrap.setBorder(new EmptyBorder(16, 20, 16, 20));
        wrap.add(inner, java.awt.BorderLayout.CENTER);
        return wrap;
    }

    public static Insets screenInsets() {
        return new Insets(16, 20, 16, 20);
    }
}
