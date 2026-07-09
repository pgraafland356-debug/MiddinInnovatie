package com.middin.innovatie.desktop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.border.Border;

/** Box-drawing border (+---+|   |+---+) for the ASCII theme. */
final class AsciiBoxBorder implements Border {
    private final Color color;
    private final Font font;

    AsciiBoxBorder(Color color, Font font) {
        this.color = color;
        this.font = font;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(color);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int lineY = y + fm.getAscent();
            int bottomY = y + height - fm.getDescent() - 1;
            int charW = Math.max(6, fm.charWidth('-'));
            int leftX = x;
            int rightX = x + width - charW;

            g2.drawString("+", leftX, lineY);
            g2.drawString("+", leftX, bottomY);
            g2.drawString("+", rightX, lineY);
            g2.drawString("+", rightX, bottomY);

            for (int px = leftX + charW; px < rightX; px += charW) {
                g2.drawString("-", px, lineY);
                g2.drawString("-", px, bottomY);
            }

            int step = Math.max(1, fm.getHeight() - 2);
            for (int py = lineY + step; py < bottomY; py += step) {
                g2.drawString("|", leftX, py);
                g2.drawString("|", rightX, py);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        FontMetrics fm = c.getFontMetrics(font);
        int pad = Math.max(8, fm.getHeight() / 2);
        return new Insets(pad + fm.getAscent(), pad + 4, pad + fm.getDescent(), pad + 4);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
