package com.salonnbooking.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import com.salonnbooking.ui.panels.SidebarPanel;
import com.salonnbooking.ui.panels.HeaderPanel;
import com.salonnbooking.ui.panels.DashboardPanel;
import com.salonnbooking.ui.panels.AppointmentPanel;
import com.salonnbooking.ui.panels.CustomerPanel;
import com.salonnbooking.ui.panels.ServicePanel;
import com.salonnbooking.ui.panels.EmployeePanel;
import com.salonnbooking.ui.panels.SettingsPanel;
import com.salonnbooking.ui.panels.BookingWizardPanel;
import com.salonnbooking.ui.panels.CheckoutPanel;
import com.salonnbooking.ui.theme.Theme;

public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private HeaderPanel headerPanel;
    private SidebarPanel sidebarPanel;

    // Flow panels
    private BookingWizardPanel bookingWizardPanel;
    private CheckoutPanel checkoutPanel;

    public MainFrame() {
        // Initialize Theme and Look and Feel properties before rendering
        Theme.setupTheme();

        setTitle("Salon Booking Manager - Hệ thống quản lý Salon chuyên nghiệp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);
        
        initUI();
    }

    private void initUI() {
        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Theme.BG_MAIN);
        
        // 1. Right panel which holds Header + Content
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.BG_MAIN);
        
        // Header
        headerPanel = new HeaderPanel();
        rightPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content Area using CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG_MAIN);
        
        // Add content panels as cards
        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new AppointmentPanel(), "appointment");
        contentPanel.add(new CustomerPanel(), "customer");
        contentPanel.add(new ServicePanel(), "service");
        contentPanel.add(new EmployeePanel(), "employee");
        contentPanel.add(new SettingsPanel(), "settings");

        bookingWizardPanel = new BookingWizardPanel();
        checkoutPanel = new CheckoutPanel();

        // Wire booking -> checkout
        bookingWizardPanel.setOnBookingConfirmed(summary -> {
            checkoutPanel.setBookingSummary(summary);
            showScreen("checkout", "Thanh toán");
        });

        contentPanel.add(bookingWizardPanel, "booking");
        contentPanel.add(checkoutPanel, "checkout");
        
        rightPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 2. Sidebar Panel with Callback BiConsumer (screenKey, screenTitle)
        sidebarPanel = new SidebarPanel((screenKey, screenTitle) -> {
            showScreen(screenKey, screenTitle);
        });
        
        // Assemble in main container
        mainContainer.add(sidebarPanel, BorderLayout.WEST);
        mainContainer.add(rightPanel, BorderLayout.CENTER);
        
        setContentPane(mainContainer);
        
        // Default to Dashboard screen
        showScreen("dashboard", "Tổng quan");
    }
    
    /**
     * Switch content panels and update the header title.
     */
    public void showScreen(String screenKey, String screenTitle) {
        cardLayout.show(contentPanel, screenKey);
        headerPanel.setScreenTitle(screenTitle);
        sidebarPanel.setActiveButton(screenKey);
    }
    
    public static void start() {
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    public static void main(String[] args) {
        start();
    }
}
