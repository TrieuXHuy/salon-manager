package com.salonnbooking.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import com.salonnbooking.ui.theme.Theme;

public class SidebarButton extends JButton {
    private boolean active = false;

    public SidebarButton(String text) {
        super(text);
        setFont(Theme.FONT_BODY_LG);
        setForeground(new Color(148, 163, 184)); // Slate text for dark background readability
        setBackground(Theme.BG_SIDEBAR);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(new EmptyBorder(12, 28, 12, 20)); // Left padding 28 to leave space for left stripe
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    setForeground(Theme.TEXT_WHITE);
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    setForeground(new Color(148, 163, 184));
                    repaint();
                }
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setForeground(Theme.TEXT_WHITE);
        } else {
            setForeground(new Color(148, 163, 184));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Paint background
        if (active) {
            g2.setColor(Theme.SIDEBAR_ACTIVE_BG);
            g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 8, 8);
            
            // Paint left stripe
            g2.setColor(Theme.EMERALD);
            g2.fillRoundRect(14, 8, 4, getHeight() - 16, 2, 2);
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(25, 41, 60)); // Subtle hover bg
            g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 8, 8);
        }
        
        super.paintComponent(g);
        g2.dispose();
    }
}
