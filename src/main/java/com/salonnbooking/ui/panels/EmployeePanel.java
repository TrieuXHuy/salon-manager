package com.salonnbooking.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.components.CircleAvatar;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class EmployeePanel extends JPanel {
    public EmployeePanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][fill, grow]"));

        // 1. Top Filter Bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm nhân viên...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

        // Role Filter
        String[] roles = {"Tất cả chức vụ", "Stylist chính", "Thợ phụ", "Thu ngân"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        filterBar.add(roleCombo, "gapright 20");

        // Add Employee Button
        JButton addButton = new JButton("+ Thêm nhân viên");
        addButton.setFont(Theme.FONT_H3);
        addButton.setForeground(Theme.TEXT_WHITE);
        addButton.setBackground(Theme.EMERALD);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBar.add(addButton, "h 38!");

        add(filterBar, "gapbottom 15");

        // 2. Staff Cards Grid Scroll Pane
        JPanel gridContainer = new JPanel(new MigLayout("wrap 3, gap 20, fillx", "[fill, 33%][fill, 33%][fill, 33%]"));
        gridContainer.setOpaque(false);

        // Add staff cards
        gridContainer.add(createStaffCard("Trần Bình", "Stylist chính", "⭐ 4.9 (142 đánh giá)", "Đang làm việc", Theme.EMERALD), "h 150!");
        gridContainer.add(createStaffCard("Lê Thảo", "Stylist chính", "⭐ 4.8 (98 đánh giá)", "Đang làm việc", Theme.EMERALD), "h 150!");
        gridContainer.add(createStaffCard("Phạm Huy", "Thợ phụ", "⭐ 4.7 (65 đánh giá)", "Đang làm việc", Theme.EMERALD), "h 150!");
        gridContainer.add(createStaffCard("Nguyễn Mai", "Thợ phụ", "⭐ 4.6 (40 đánh giá)", "Nghỉ phép", Theme.AMBER), "h 150!");
        gridContainer.add(createStaffCard("Hoàng Kim", "Thu ngân", "⭐ 5.0 (21 đánh giá)", "Đang làm việc", Theme.EMERALD), "h 150!");
        gridContainer.add(createStaffCard("Đỗ Quốc Anh", "Stylist chính", "⭐ 4.9 (110 đánh giá)", "Ngoại tuyến", Theme.SLATE), "h 150!");

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        add(scrollPane, "grow");
    }

    private RoundedPanel createStaffCard(String name, String role, String rating, String status, Color statusColor) {
        RoundedPanel card = new RoundedPanel(12, Theme.BG_CARD, true);
        card.setLayout(new MigLayout("insets 16, fillx", "[]16[grow]", "[]"));

        // Left side: CircleAvatar
        CircleAvatar avatar = new CircleAvatar(name, 56, Theme.NAVY, Theme.TEXT_WHITE);
        card.add(avatar, "aligny top");

        // Right side: Info Panel
        JPanel infoPanel = new JPanel(new MigLayout("insets 0, gap 4, fillx", "[fill]", "[]"));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(Theme.FONT_H2);
        nameLabel.setForeground(Theme.NAVY);
        infoPanel.add(nameLabel, "wrap");

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(Theme.FONT_BODY_LG);
        roleLabel.setForeground(Theme.TEXT_MUTED);
        infoPanel.add(roleLabel, "wrap");

        JLabel ratingLabel = new JLabel(rating);
        ratingLabel.setFont(Theme.FONT_BODY_SM);
        ratingLabel.setForeground(Theme.AMBER);
        infoPanel.add(ratingLabel, "wrap, gapbottom 4");

        // Status Badge
        BadgeLabel statusBadge = new BadgeLabel(status, statusColor, Theme.TEXT_WHITE);
        infoPanel.add(statusBadge, "w min!, h 24!");

        card.add(infoPanel, "growx");
        return card;
    }

    private static class BadgeLabel extends JLabel {
        private final Color bgColor;
        
        public BadgeLabel(String text, Color bgColor, Color textColor) {
            super(text);
            this.bgColor = bgColor;
            setForeground(textColor);
            setFont(Theme.FONT_BODY_SM.deriveFont(java.awt.Font.BOLD));
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}
