package com.salonnbooking;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.salonnbooking.desktop.ui.auth.AuthFrame;
import com.salonnbooking.ui.theme.Theme;

public class SwingClient {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        Theme.setupTheme();

        SwingUtilities.invokeLater(() -> {
            try {
                AuthFrame authFrame = new AuthFrame();
                authFrame.setVisible(true);
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
