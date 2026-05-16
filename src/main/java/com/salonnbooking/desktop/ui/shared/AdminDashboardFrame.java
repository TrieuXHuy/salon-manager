package com.salonnbooking.desktop.ui.shared;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class AdminDashboardFrame extends MainFrame {

    public AdminDashboardFrame() {
        super("Salon Booking Manager - ADMIN");

        addScreen("dashboard", "Dashboard", simplePlaceholder("Admin Dashboard"));
        addScreen("users", "Users", simplePlaceholder("User Management (TODO)"));
        addScreen("serviceCategories", "Service Categories", simplePlaceholder("Service Categories (TODO)"));
        addScreen("services", "Services", simplePlaceholder("Service Management (TODO)"));
        addScreen("appointments", "Appointments", simplePlaceholder("Appointment Management (TODO)"));
        addScreen("payments", "Payments", simplePlaceholder("Payment Panel (TODO)"));

        showScreen("dashboard");
    }

    private JPanel simplePlaceholder(String text) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(text));
        return panel;
    }
}
