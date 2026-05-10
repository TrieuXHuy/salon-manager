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
        setFont(Theme.FONT_MEDIUM);
        setForeground(Theme.TEXT_MUTED);
        setBackground(Theme.BG_SIDEBAR);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(new EmptyBorder(12, 20, 12, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    setBackground(Theme.ACCENT);
                    setForeground(Theme.PRIMARY);
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    setBackground(Theme.BG_SIDEBAR);
                    setForeground(Theme.TEXT_MUTED);
                    repaint();
                }
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setBackground(Theme.PRIMARY);
            setForeground(Theme.TEXT_WHITE);
        } else {
            setBackground(Theme.BG_SIDEBAR);
            setForeground(Theme.TEXT_MUTED);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (active || getModel().isRollover()) {
            g2.setColor(getBackground());
            g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 10, 10);
        }
        
        super.paintComponent(g);
        g2.dispose();
    }
}
