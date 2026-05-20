package com.salonnbooking.desktop.ui.shared;

public class StaffDashboardFrame extends MainFrame {

    public StaffDashboardFrame() {
        super("Salon Booking Manager - STAFF");

        addScreen("appointments", "Lịch hẹn", new com.salonnbooking.ui.panels.AppointmentPanel());
        addScreen("customers", "Khách hàng", new com.salonnbooking.ui.panels.CustomerPanel());
        addScreen("services", "Dịch vụ", new com.salonnbooking.ui.panels.ServicePanel());

        showScreen("appointments");
    }
}
