package com.middin.innovatie.desktop;

import com.middin.innovatie.desktop.ui.ThemeManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new DesktopApp().show();
        });
    }

    private Main() {}
}
