package com.salonnbooking.ui.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.salonnbooking.ui.components.SidebarButton;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class SidebarPanel extends JPanel {
    private final BiConsumer<String, String> onMenuSelected;
    private final Map<String, SidebarButton> buttons = new HashMap<>();

    public SidebarPanel(BiConsumer<String, String> onMenuSelected) {
        this.onMenuSelected = onMenuSelected;
        
        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(260, 0));
        setLayout(new MigLayout("wrap 1, fillx, insets 24 12 24 12", "[fill]", "[][]push[]"));

        // 1. Logo / Title Section
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 30, 16));

        JLabel titleLabel = new JLabel("SalonManager");
        titleLabel.setFont(Theme.FONT_HERO.deriveFont(22f)); // Scaled brand title
        titleLabel.setForeground(Theme.EMERALD);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Hệ Thống Đặt Lịch & Quản Lý");
        subtitleLabel.setFont(Theme.FONT_BODY_SM);
        subtitleLabel.setForeground(new Color(148, 163, 184)); // Slate text
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitleLabel);
        add(logoPanel, "growx");

        // 2. Navigation Menu
        JPanel menuPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 0, gap 6", "[fill]"));
        menuPanel.setOpaque(false);

        // Menu definitions: Key -> {Label, ScreenTitle}
        createMenuItem(menuPanel, "dashboard", "Dashboard", "Tổng quan");
        createMenuItem(menuPanel, "appointment", "Lịch hẹn", "Quản lý Lịch Hẹn");
        createMenuItem(menuPanel, "booking", "Đặt lịch (Wizard)", "Đặt lịch");
        createMenuItem(menuPanel, "customer", "Khách hàng", "Quản lý Khách Hàng");
        createMenuItem(menuPanel, "service", "Dịch vụ", "Quản lý Dịch Vụ");
        createMenuItem(menuPanel, "employee", "Nhân viên", "Quản lý Nhân Viên");
        createMenuItem(menuPanel, "settings", "Cấu hình", "Cấu hình hệ thống");

        add(menuPanel, "growx");
    }

    private void createMenuItem(JPanel parent, String key, String label, String screenTitle) {
        SidebarButton btn = new SidebarButton(label);
        btn.addActionListener(e -> {
            if (onMenuSelected != null) {
                onMenuSelected.accept(key, screenTitle);
            }
        });
        buttons.put(key, btn);
        parent.add(btn, "h 44!");
    }

    /**
     * Highlights the selected sidebar button and de-selects all other buttons.
     */
    public void setActiveButton(String screenKey) {
        buttons.forEach((key, btn) -> btn.setActive(key.equals(screenKey)));
    }
}
