package com.salonnbooking;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.salonnbooking.desktop.ui.auth.AuthFrame;

public class SwingClient {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        FlatLightLaf.setup();
        configureLightPalette();

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

    private static void configureLightPalette() {
        UIManager.put("Panel.background", new java.awt.Color(248, 250, 252));
        UIManager.put("Table.background", java.awt.Color.WHITE);
        UIManager.put("Table.alternateRowColor", new java.awt.Color(245, 247, 250));
        UIManager.put("Table.selectionBackground", new java.awt.Color(219, 234, 254));
        UIManager.put("Table.selectionForeground", new java.awt.Color(15, 23, 42));
        UIManager.put("TableHeader.background", new java.awt.Color(239, 246, 255));
        UIManager.put("TableHeader.foreground", new java.awt.Color(30, 41, 59));
        UIManager.put("Component.borderColor", new java.awt.Color(203, 213, 225));
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
    }
}
