package com.salonnbooking;

import javax.swing.*;

import com.salonnbooking.ui.MainDashboard;
import com.salonnbooking.ui.panel.AppointmentPanel;
import com.salonnbooking.ui.panel.CustomerPanel;
import com.salonnbooking.ui.panel.ServicePanel;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;

/**
 * SwingClient - Entry point của ứng dụng Java Swing
 * Kết nối với Spring Boot Backend thông qua ApiClient
 * 
 * Cấu trúc:
 * - MainDashboard: Khung sườn chính với BorderLayout & CardLayout
 * - CustomerPanel: Quản lý khách hàng
 * - AppointmentPanel: Quản lý lịch hẹn
 * - ServicePanel: Quản lý dịch vụ
 */
public class SwingClient {

	public static void main(String[] args) {
		// Setup FlatLaf theme trước khi tạo UI
		FlatDarkPurpleIJTheme.setup();

		// Chạy UI trên Event Dispatch Thread
		SwingUtilities.invokeLater(() -> {
			try {
				// Tạo main dashboard
				MainDashboard dashboard = new MainDashboard();

				// Tạo các panel
				CustomerPanel customerPanel = new CustomerPanel();
				AppointmentPanel appointmentPanel = new AppointmentPanel();
				ServicePanel servicePanel = new ServicePanel();

				// Thêm các panel vào dashboard
				dashboard.addPanel(MainDashboard.PANEL_CUSTOMER, customerPanel);
				dashboard.addPanel(MainDashboard.PANEL_APPOINTMENT, appointmentPanel);
				dashboard.addPanel(MainDashboard.PANEL_SERVICE, servicePanel);

				// Hiển thị dashboard
				dashboard.setVisible(true);
				dashboard.showPanel(MainDashboard.PANEL_CUSTOMER);

			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Error initializing application: " + e.getMessage(),
						"Startup Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}
}
