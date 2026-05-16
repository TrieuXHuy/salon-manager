package com.salonnbooking.desktop.ui.shared;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StaffDashboardFrame extends MainFrame {

    public StaffDashboardFrame() {
        super("Salon Booking Manager - STAFF");

        addScreen("appointments", "Appointments", simplePlaceholder("Staff Appointments (TODO)"));
        addScreen("schedule", "Working Hours", simplePlaceholder("Staff Schedule (TODO)"));
        addScreen("customers", "Customers", simplePlaceholder("Customer Info (TODO)"));

        showScreen("appointments");
    }

    private JPanel simplePlaceholder(String text) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(text));
        return panel;
    }
}
