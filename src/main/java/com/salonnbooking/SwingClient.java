package com.salonnbooking;

import com.salonnbooking.ui.theme.Theme;

import javax.swing.*;

public class SwingClient {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        Theme.setupTheme();

        SwingUtilities.invokeLater(() -> {
            try {
                com.salonnbooking.ui.LoginFrame loginFrame = new com.salonnbooking.ui.LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
