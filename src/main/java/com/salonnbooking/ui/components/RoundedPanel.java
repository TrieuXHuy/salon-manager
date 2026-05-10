package com.salonnbooking.ui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import java.awt.Color;

public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private boolean hasShadow;

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, false);
    }

    public RoundedPanel(int radius, Color bgColor, boolean shadow) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.hasShadow = shadow;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hasShadow) {
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius);
        }
        
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - (hasShadow ? 2 : 0), getHeight() - (hasShadow ? 2 : 0), cornerRadius, cornerRadius);
        g2.dispose();
    }
}
