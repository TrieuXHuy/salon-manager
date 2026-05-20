package com.salonnbooking.desktop.ui.shared;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Cursor;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.MatteBorder;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.salonnbooking.desktop.session.AuthSession;
import com.salonnbooking.desktop.ui.auth.AuthFrame;
import com.salonnbooking.ui.theme.Theme;
import com.salonnbooking.ui.components.SidebarButton;
import com.salonnbooking.ui.components.CircleAvatar;
import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel sidebarPanel = new JPanel();
    private final JPanel menuPanel = new JPanel();
    private final JLabel screenTitleLabel = new JLabel();
    private final JLabel userNameLabel = new JLabel();
    private final JLabel userRoleLabel = new JLabel();
    private CircleAvatar avatarComponent;
    private final JPanel headerProfilePanel = new JPanel();

    private final Map<String, SidebarButton> sidebarButtons = new LinkedHashMap<>();
    private final Map<String, JPanel> screens = new LinkedHashMap<>();
    private final Map<String, String> screenTitles = new LinkedHashMap<>();

    public MainFrame(String title) {
        super(title);

        // Apply theme settings
        Theme.setupTheme();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1250, 800));
        setLocationRelativeTo(null);

        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Theme.BG_MAIN);

        // 1. Setup Sidebar (Left, Width 260px, Navy Theme)
        sidebarPanel.setBackground(Theme.BG_SIDEBAR);
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new MigLayout("wrap 1, fillx, insets 24 12 24 12", "[fill]", "[][]push"));

        // Brand section
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 30, 16));

        JLabel titleLabel = new JLabel("SalonManager");
        titleLabel.setFont(Theme.FONT_HERO.deriveFont(22f));
        titleLabel.setForeground(Theme.EMERALD);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Hệ Thống Đặt Lịch & Quản Lý");
        subtitleLabel.setFont(Theme.FONT_BODY_SM);
        subtitleLabel.setForeground(new java.awt.Color(148, 163, 184));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitleLabel);
        sidebarPanel.add(logoPanel, "growx");

        // Menu container
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new MigLayout("wrap 1, fillx, insets 0, gap 6", "[fill]"));
        sidebarPanel.add(menuPanel, "growx");

        // 2. Right panel (Header + CardLayout Content)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.BG_MAIN);

        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("insets 16 30 16 30, fillx", "[left]push[right]"));
        headerPanel.setBackground(Theme.BG_CARD);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        screenTitleLabel.setFont(Theme.FONT_H1);
        screenTitleLabel.setForeground(Theme.NAVY);
        headerPanel.add(screenTitleLabel);

        // Profile details container
        headerProfilePanel.setOpaque(false);
        headerProfilePanel.setLayout(new MigLayout("insets 0, gap 16", "[][]"));

        userNameLabel.setFont(Theme.FONT_H3);
        userNameLabel.setForeground(Theme.TEXT_PRIMARY);
        userRoleLabel.setFont(Theme.FONT_BODY_SM);
        userRoleLabel.setForeground(Theme.TEXT_MUTED);

        // Refresh dynamic user details
        refreshUserLabel();

        headerPanel.add(headerProfilePanel);
        rightPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel
        contentPanel.setBackground(Theme.BG_MAIN);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        // Assemble main container
        mainContainer.add(sidebarPanel, BorderLayout.WEST);
        mainContainer.add(rightPanel, BorderLayout.CENTER);
        setContentPane(mainContainer);
    }

    public void addScreen(String key, String label, JPanel panel) {
        screens.put(key, panel);
        screenTitles.put(key, label);
        
        // Wrap screen in a beautiful container with some margins
        JPanel wrappedPanel = new JPanel(new BorderLayout());
        wrappedPanel.setBackground(Theme.BG_MAIN);
        wrappedPanel.add(panel, BorderLayout.CENTER);
        
        contentPanel.add(wrappedPanel, key);

        // Add to sidebar buttons
        SidebarButton button = new SidebarButton(label);
        button.addActionListener(e -> showScreen(key));
        sidebarButtons.put(key, button);
        menuPanel.add(button, "h 44!");
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    public void showScreen(String key) {
        cardLayout.show(contentPanel, key);
        
        // Update active states
        sidebarButtons.forEach((k, btn) -> btn.setActive(k.equals(key)));
        
        // Update screen title
        String title = screenTitles.get(key);
        if (title != null) {
            screenTitleLabel.setText(title);
        }
    }

    public void refreshUserLabel() {
        AuthSession session = AuthSession.getInstance();
        String name = session.getFullName() != null ? session.getFullName() : "Admin";
        String roleStr = session.getRole() != null ? session.getRole().name() : "ADMIN";
        
        userNameLabel.setText(name);
        userRoleLabel.setText(roleStr.equals("ADMIN") ? "Quản trị viên" : roleStr.equals("STAFF") ? "Nhân viên" : "Khách hàng");

        // Clear and rebuild profile panel items to ensure layout order
        headerProfilePanel.removeAll();
        
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(userNameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(userRoleLabel);
        headerProfilePanel.add(textPanel);
        
        avatarComponent = new CircleAvatar(name, 40, Theme.EMERALD, Theme.TEXT_WHITE);
        headerProfilePanel.add(avatarComponent);
        
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(Theme.FONT_BODY_LG);
        logoutButton.setForeground(Theme.CRIMSON);
        logoutButton.setBackground(new java.awt.Color(254, 226, 226));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> logout());
        headerProfilePanel.add(logoutButton, "h 34!");
        
        headerProfilePanel.revalidate();
        headerProfilePanel.repaint();
    }

    private void logout() {
        AuthSession.getInstance().clear();
        SwingUtilities.invokeLater(() -> {
            dispose();
            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);
        });
    }
}
