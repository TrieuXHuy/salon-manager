package com.salonnbooking.ui;

import com.salonnbooking.ui.panels.AppointmentPanel;
import com.salonnbooking.ui.panels.BookingWizardPanel;
import com.salonnbooking.ui.panels.CheckoutPanel;
import com.salonnbooking.ui.panels.CustomerPanel;
import com.salonnbooking.ui.panels.DashboardPanel;
import com.salonnbooking.ui.panels.EmployeePanel;
import com.salonnbooking.ui.panels.HeaderPanel;
import com.salonnbooking.ui.panels.ServicePanel;
import com.salonnbooking.ui.panels.SettingsPanel;
import com.salonnbooking.ui.panels.SidebarPanel;
import com.salonnbooking.ui.theme.Theme;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private HeaderPanel headerPanel;
    private SidebarPanel sidebarPanel;

    private BookingWizardPanel bookingWizardPanel;
    private CheckoutPanel checkoutPanel;

    public MainFrame() {
        Theme.setupTheme();

        setTitle("Salon Booking Manager - Hệ thống quản lý Salon chuyên nghiệp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Theme.BG_MAIN);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.BG_MAIN);

        headerPanel = new HeaderPanel();
        rightPanel.add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG_MAIN);

        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new AppointmentPanel(), "appointment");
        contentPanel.add(new CustomerPanel(), "customer");
        contentPanel.add(new ServicePanel(), "service");
        contentPanel.add(new EmployeePanel(), "employee");
        contentPanel.add(new SettingsPanel(), "settings");

        bookingWizardPanel = new BookingWizardPanel();
        checkoutPanel = new CheckoutPanel();

        bookingWizardPanel.setOnBookingConfirmed(summary -> {
            checkoutPanel.setBookingSummary(summary);
            showScreen("checkout", "Thanh toán");
        });

        contentPanel.add(bookingWizardPanel, "booking");
        contentPanel.add(checkoutPanel, "checkout");

        rightPanel.add(contentPanel, BorderLayout.CENTER);

        sidebarPanel = new SidebarPanel(this::showScreen);

        mainContainer.add(sidebarPanel, BorderLayout.WEST);
        mainContainer.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainContainer);

        // Allow panels to request navigation without having a MainFrame reference.
        ScreenRouter.setNavigator(this::showScreen);

        showScreen("dashboard", "Tổng quan");
    }

    public void showScreen(String screenKey, String screenTitle) {
        cardLayout.show(contentPanel, screenKey);
        headerPanel.setScreenTitle(screenTitle);
        sidebarPanel.setActiveButton(screenKey);
    }

    public static void start() {
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    public static void main(String[] args) {
        start();
    }
}

