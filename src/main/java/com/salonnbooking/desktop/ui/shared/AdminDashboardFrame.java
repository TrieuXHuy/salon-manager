package com.salonnbooking.desktop.ui.shared;

public class AdminDashboardFrame extends MainFrame {

    public AdminDashboardFrame() {
        super("Salon Booking Manager - ADMIN");

        addScreen("dashboard", "Dashboard", new com.salonnbooking.ui.panels.DashboardPanel());
        addScreen("appointments", "Lịch hẹn", new com.salonnbooking.ui.panels.AppointmentPanel());
        addScreen("customers", "Khách hàng", new com.salonnbooking.ui.panels.CustomerPanel());
        addScreen("services", "Dịch vụ", new com.salonnbooking.ui.panels.ServicePanel());
        addScreen("employees", "Nhân viên", new com.salonnbooking.ui.panels.EmployeePanel());
        addScreen("settings", "Cấu hình", new com.salonnbooking.ui.panels.SettingsPanel());

        showScreen("dashboard");
    }
}
