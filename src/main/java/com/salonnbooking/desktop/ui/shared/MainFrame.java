package com.salonnbooking.desktop.ui.shared;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.salonnbooking.desktop.session.AuthSession;
import com.salonnbooking.desktop.ui.auth.AuthFrame;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel sidebar = new JPanel();
    private final JLabel userLabel = new JLabel();

    private final Map<String, JPanel> screens = new LinkedHashMap<>();

    public MainFrame(String title) {
        super(title);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel topbar = new JPanel(new BorderLayout(8, 0));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        userLabel.setText(buildUserText());

        topbar.add(userLabel, BorderLayout.WEST);
        topbar.add(logoutButton, BorderLayout.EAST);

        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.add(topbar, BorderLayout.NORTH);
        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    public void addScreen(String key, String label, JPanel panel) {
        screens.put(key, panel);
        contentPanel.add(panel, key);

        JButton button = new JButton(label);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.addActionListener(e -> showScreen(key));
        sidebar.add(button);
    }

    public void showScreen(String key) {
        cardLayout.show(contentPanel, key);
    }

    public void refreshUserLabel() {
        userLabel.setText(buildUserText());
    }

    private String buildUserText() {
        AuthSession session = AuthSession.getInstance();
        String name = session.getFullName() != null ? session.getFullName() : "Unknown";
        String role = session.getRole() != null ? session.getRole().name() : "UNKNOWN";
        return "User: " + name + " (" + role + ")";
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
