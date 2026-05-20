package com.salonnbooking.desktop.ui.shared;

import com.salonnbooking.ui.panels.AppointmentPanel;
import com.salonnbooking.ui.panels.BookingWizardPanel;
import com.salonnbooking.ui.panels.CheckoutPanel;
import com.salonnbooking.ui.panels.CustomerPanel;
import com.salonnbooking.ui.panels.DashboardPanel;
import com.salonnbooking.ui.panels.EmployeePanel;
import com.salonnbooking.ui.panels.ServicePanel;
import com.salonnbooking.ui.panels.SettingsPanel;

public class AdminDashboardFrame extends MainFrame {

    public AdminDashboardFrame() {
        super("Salon Booking Manager - ADMIN");

        addScreen("dashboard", "Dashboard", new DashboardPanel());
        AppointmentPanel appointmentPanel = new AppointmentPanel();
        addScreen("appointments", "Lịch hẹn", appointmentPanel);

        // Booking flow (opened from "+ Đặt lịch mới" in AppointmentPanel)
        BookingWizardPanel bookingWizardPanel = new BookingWizardPanel();
        bookingWizardPanel.setOnBookingConfirmed(summary -> appointmentPanel.loadAppointments());
        addScreen("booking", "Đặt lịch", bookingWizardPanel);
        addScreen("checkout", "Thanh toán", new CheckoutPanel());

        addScreen("customers", "Khách hàng", new CustomerPanel());
        addScreen("services", "Dịch vụ", new ServicePanel());
        addScreen("employees", "Nhân viên", new EmployeePanel());
        addScreen("settings", "Cấu hình", new SettingsPanel());

        showScreen("dashboard");
    }
}
