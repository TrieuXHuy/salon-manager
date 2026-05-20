package com.salonnbooking.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import com.salonnbooking.ui.theme.Theme;

public class CircleAvatar extends JPanel {
    private final String initials;
    private final Color bgColor;
    private final Color textColor;
    private final int size;

    public CircleAvatar(String text, int size) {
        this(text, size, Theme.NAVY, Theme.TEXT_WHITE);
    }

    public CircleAvatar(String text, int size, Color bgColor, Color textColor) {
        this.initials = getInitials(text);
        this.size = size;
        this.bgColor = bgColor;
        this.textColor = textColor;

        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setOpaque(false);
    }

    private String getInitials(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "??";
        }
        text = text.trim();
        String[] parts = text.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background circle
        g2.setColor(bgColor);
        g2.fillOval(0, 0, getWidth(), getHeight());

        // Draw text initials
        g2.setColor(textColor);
        
        // Scale font according to size
        int fontSize = Math.max(10, size / 2 - 2);
        g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getAscent();
        
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() + textHeight) / 2 - 2;
        
        g2.drawString(initials, x, y);
        g2.dispose();
    }
}
