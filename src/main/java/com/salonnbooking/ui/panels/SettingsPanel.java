package com.salonnbooking.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Cursor;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class SettingsPanel extends JPanel {
    public SettingsPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[]"));

        // Main Card Container (12px border radius, subtle shadows)
        RoundedPanel settingsCard = new RoundedPanel(12, Theme.BG_CARD, true);
        // Dual column form layout: right column grows
        settingsCard.setLayout(new MigLayout("insets 30, gapy 20, fillx", "[right, 180!]20[grow]"));

        // Header Section
        JLabel sectionTitle = new JLabel("📋 CẤU HÌNH HỆ THỐNG SALON");
        sectionTitle.setFont(Theme.FONT_H2);
        sectionTitle.setForeground(Theme.NAVY);
        settingsCard.add(sectionTitle, "span 2, wrap, gapbottom 10");

        // 1. Salon Branding
        settingsCard.add(new JLabel("Tên Salon:"), "");
        JTextField salonName = new JTextField("Salon Pro - Chăm Sóc Tóc Chuyên Nghiệp");
        settingsCard.add(salonName, "growx, wrap");

        settingsCard.add(new JLabel("Số điện thoại:"), "");
        JTextField salonPhone = new JTextField("0900 123 456");
        settingsCard.add(salonPhone, "growx, wrap");

        settingsCard.add(new JLabel("Địa chỉ:"), "");
        JTextField salonAddress = new JTextField("123 Đường Ba Tháng Hai, Quận 10, TP. Hồ Chí Minh");
        settingsCard.add(salonAddress, "growx, wrap");

        // 2. Operational Hours
        settingsCard.add(new JLabel("Giờ mở cửa:"), "");
        String[] hours = {"08:00", "08:30", "09:00", "09:30", "10:00"};
        JComboBox<String> openCombo = new JComboBox<>(hours);
        openCombo.setSelectedItem("09:00");
        settingsCard.add(openCombo, "w 150!, wrap");

        settingsCard.add(new JLabel("Giờ đóng cửa:"), "");
        String[] closeHours = {"20:00", "20:30", "21:00", "21:30", "22:00"};
        JComboBox<String> closeCombo = new JComboBox<>(closeHours);
        closeCombo.setSelectedItem("21:00");
        settingsCard.add(closeCombo, "w 150!, wrap");

        // 3. Theme & Look and Feel
        settingsCard.add(new JLabel("Giao diện mặc định:"), "");
        String[] themes = {"FlatLaf Light (IntelliJ)", "FlatLaf Dark (Darcula)"};
        JComboBox<String> themeCombo = new JComboBox<>(themes);
        settingsCard.add(themeCombo, "w 220!, wrap");

        // 4. Save Buttons Bar
        JPanel btnBar = new JPanel(new MigLayout("insets 0, gap 12", "[]"));
        btnBar.setOpaque(false);

        JButton saveBtn = new JButton("Lưu cấu hình");
        saveBtn.setFont(Theme.FONT_H3);
        saveBtn.setForeground(Theme.TEXT_WHITE);
        saveBtn.setBackground(Theme.EMERALD);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Đã lưu các thiết lập cấu hình salon thành công!", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
        });

        JButton resetBtn = new JButton("Khôi phục mặc định");
        resetBtn.setFont(Theme.FONT_BODY_LG);
        resetBtn.setForeground(Theme.TEXT_PRIMARY);
        resetBtn.setBackground(Theme.BORDER);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBar.add(saveBtn, "h 38!");
        btnBar.add(resetBtn, "h 38!");

        settingsCard.add(new JLabel(""), ""); // spacer
        settingsCard.add(btnBar, "gapt 15, wrap");

        add(settingsCard, "growx");
    }
}
