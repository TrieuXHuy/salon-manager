package com.salonnbooking;

import com.salonnbooking.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

public class SwingClient {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        // Enable DPI scaling for high-resolution displays on Linux
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.uiScale", "1.0");
        
        // Initialize DPI-aware scaling
        initializeDPIScaling();

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

    private static void initializeDPIScaling() {
        // Get the default toolkit to detect DPI
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int dpi = toolkit.getScreenResolution();
        
        // Standard DPI on most displays is 96
        float scaleFactor = dpi / 96f;
        
        // Set scaling factor in Theme for UI components to use
        Theme.setDPIScaleFactor(scaleFactor);
        
        // Log DPI info for debugging
        System.out.println("Screen DPI: " + dpi);
        System.out.println("UI Scale Factor: " + scaleFactor);
    }
}
