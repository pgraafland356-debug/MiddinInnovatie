package com.middin.innovatie.desktop.ui;

/** Corner shape for buttons/cards (Instellingen → Vorm). */
public enum UiShapeId {
    ROUNDED("rounded", "Afgerond", 12),
    SQUARE("square", "Vierkant", 0),
    SOFT("soft", "Zacht (meer rond)", 20),
    PILL("pill", "Pilvorm", 999);

    private final String id;
    private final String label;
    private final int arcHint;

    UiShapeId(String id, String label, int arcHint) {
        this.id = id;
        this.label = label;
        this.arcHint = arcHint;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    /** Base corner radius; PILL is resolved against component height at paint time. */
    public int arcHint() {
        return arcHint;
    }

    public int resolveArc(int width, int height) {
        if (this == PILL) {
            return Math.max(width, height);
        }
        return Math.max(0, arcHint);
    }

    public static UiShapeId fromId(String raw) {
        if (raw == null || raw.isBlank()) return ROUNDED;
        String t = raw.trim().toLowerCase();
        for (UiShapeId shape : values()) {
            if (shape.id.equals(t)) return shape;
        }
        return ROUNDED;
    }
}
