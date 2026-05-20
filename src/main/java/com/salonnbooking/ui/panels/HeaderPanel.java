package com.salonnbooking.ui.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.MatteBorder;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

import com.salonnbooking.ui.components.CircleAvatar;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class HeaderPanel extends JPanel {
    private final JLabel titleLabel;

    public HeaderPanel() {
        setBackground(Theme.BG_CARD);
        setLayout(new MigLayout("insets 16 30 16 30, fillx", "[left]push[right]"));
        setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Screen title
        titleLabel = new JLabel("Tổng quan");
        titleLabel.setFont(Theme.FONT_H1);
        titleLabel.setForeground(Theme.NAVY);
        add(titleLabel);

        // Right-aligned User Profile Section
        JPanel profileContainer = new JPanel();
        profileContainer.setOpaque(false);
        profileContainer.setLayout(new MigLayout("insets 0, gap 16", "[][]"));

        // User info text
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Nguyễn Anh Tuấn");
        nameLabel.setFont(Theme.FONT_H3);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel roleLabel = new JLabel("Quản trị viên");
        roleLabel.setFont(Theme.FONT_BODY_SM);
        roleLabel.setForeground(Theme.TEXT_MUTED);

        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(roleLabel);
        profileContainer.add(textPanel);

        // Simple Avatar
        CircleAvatar avatar = new CircleAvatar("Nguyễn Anh Tuấn", 40, Theme.EMERALD, Theme.TEXT_WHITE);
        profileContainer.add(avatar);

        // Logout Button
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(Theme.FONT_BODY_LG);
        logoutButton.setForeground(Theme.CRIMSON);
        logoutButton.setBackground(new java.awt.Color(254, 226, 226)); // Light crimson bg
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            System.out.println("Đăng xuất thành công");
        });
        profileContainer.add(logoutButton, "h 34!");

        add(profileContainer);
    }

    /**
     * Updates the screen title display.
     */
    public void setScreenTitle(String title) {
        titleLabel.setText(title);
    }
}
