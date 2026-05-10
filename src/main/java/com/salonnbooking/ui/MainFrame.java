package com.salonnbooking.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.salonnbooking.ui.panels.SidebarPanel;
import com.salonnbooking.ui.panels.HeaderPanel;
import com.salonnbooking.ui.panels.DashboardPanel;
import com.salonnbooking.ui.theme.Theme;
import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("SalonManager - Hệ thống quản lý Salon chuyên nghiệp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        initUI();
    }

    private void initUI() {
        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Theme.BG_MAIN);
        
        // Sidebar
        SidebarPanel sidebar = new SidebarPanel();
        mainContainer.add(sidebar, BorderLayout.WEST);
        
        // Right side (Header + Content)
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        HeaderPanel header = new HeaderPanel();
        rightPanel.add(header, BorderLayout.NORTH);
        
        // Content Area (Dashboard by default)
        DashboardPanel dashboard = new DashboardPanel();
        rightPanel.add(dashboard, BorderLayout.CENTER);
        
        mainContainer.add(rightPanel, BorderLayout.CENTER);
        
        setContentPane(mainContainer);
    }
    
    public static void start() {
        // Apply FlatLaf for modern baseline styling
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    public static void main(String[] args) {
        start();
    }
}
