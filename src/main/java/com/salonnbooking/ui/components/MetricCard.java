package com.salonnbooking.ui.components;

import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MetricCard extends RoundedPanel {

    private final Color defaultBg = Theme.BG_CARD;
    private final Color hoverBg = new Color(248, 250, 252); // Slate 50
    private final Color defaultBorder = new Color(241, 245, 249); // Slate 100
    private final Color hoverBorder = Theme.EMERALD;

    private boolean isHovered = false;

    private final JLabel valLabel;

    private static int s(int value) {
        return Theme.scaleDimension(value);
    }

    private static int gap(int value) {
        float scale = Math.min(Theme.getDPIScaleFactor(), 1.25f);
        return Math.round(value * scale);
    }

    public MetricCard(String title, String value, String icon, Color iconBg, Color iconFg) {
        super(gap(16), Theme.BG_CARD, true);
        
        setLayout(new MigLayout("insets " + gap(16) + ", fill", "[][grow]", "[center]"));
        setMinimumSize(new Dimension(0, s(96)));
        setPreferredSize(new Dimension(s(190), s(104)));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 1. Icon Badge Panel
        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(s(48), s(48)));
        iconBadge.setLayout(new MigLayout("fill, insets 0", "[center]", "[center]"));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(Theme.FONT_H2);
        iconLabel.setForeground(iconFg);
        iconBadge.add(iconLabel);

        add(iconBadge, "width " + s(48) + "!, height " + s(48) + "!, gapright " + gap(12));

        // 2. Text Details
        JPanel textPanel = new JPanel(new MigLayout("wrap 1, insets 0, gapy " + gap(2), "[fill]"));
        textPanel.setOpaque(false);

        valLabel = new JLabel(value);
        valLabel.setFont(Theme.FONT_H2);
        valLabel.setForeground(Theme.NAVY);
        textPanel.add(valLabel);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_BODY_SM);
        titleLabel.setForeground(Theme.TEXT_MUTED);
        textPanel.add(titleLabel);

        add(textPanel, "growx");

        // Hover listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setBackground(hoverBg);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setBackground(defaultBg);
                repaint();
            }
        });
    }

    public void setValue(String value) {
        valLabel.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw hover border
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isHovered ? hoverBorder : defaultBorder);
        g2.setStroke(new java.awt.BasicStroke(isHovered ? 1.5f : 1.0f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
        g2.dispose();
    }
}
