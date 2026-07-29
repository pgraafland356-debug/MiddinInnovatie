package com.middin.innovatie.desktop.ui;

/** Where main navigation buttons are placed (Instellingen → Knoppenlayout). */
public enum ButtonLayoutId {
    BOTTOM("bottom", "Onder (balk)"),
    LEFT("left", "Links (zijbalk)"),
    TOP("top", "Boven (tabs)");

    private final String id;
    private final String label;

    ButtonLayoutId(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static ButtonLayoutId fromId(String raw) {
        if (raw == null || raw.isBlank()) return BOTTOM;
        String t = raw.trim().toLowerCase();
        for (ButtonLayoutId layout : values()) {
            if (layout.id.equals(t)) return layout;
        }
        return BOTTOM;
    }
}
