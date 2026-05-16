package com.salonnbooking.desktop.ui.shared;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.salonnbooking.desktop.ui.customer.ServiceListPanel;

public class CustomerDashboardFrame extends MainFrame {

    public CustomerDashboardFrame() {
        super("Salon Booking Manager - CUSTOMER");

        addScreen("services", "Services", new ServiceListPanel());
        addScreen("booking", "Booking", simplePlaceholder("Booking (TODO)"));
        addScreen("appointments", "My Appointments", simplePlaceholder("My Appointments (TODO)"));
        addScreen("profile", "Profile", simplePlaceholder("Profile (TODO)"));

        showScreen("services");
    }

    private JPanel simplePlaceholder(String text) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(text));
        return panel;
    }
}
