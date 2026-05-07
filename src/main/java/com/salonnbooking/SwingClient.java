package com.salonnbooking;

import javax.swing.*;

import com.salonnbooking.ui.MainDashboard;
import com.salonnbooking.ui.panel.AppointmentPanel;
import com.salonnbooking.ui.panel.CustomerPanel;
import com.salonnbooking.ui.panel.DashboardPanel;
import com.salonnbooking.ui.panel.ReportPanel;
import com.salonnbooking.ui.panel.ServicePanel;
import com.formdev.flatlaf.FlatLightLaf;

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
		// Cấu hình UTF-8 encoding cho JVM
		System.setProperty("file.encoding", "UTF-8");
		System.setProperty("sun.jnu.encoding", "UTF-8");
		
		// Setup FlatLaf theme trước khi tạo UI
		FlatLightLaf.setup();
		configureLightPalette();

		// Chạy UI trên Event Dispatch Thread
		SwingUtilities.invokeLater(() -> {
			try {
				// Tạo main dashboard
				MainDashboard dashboard = new MainDashboard();

				// Tạo các panel
				DashboardPanel dashboardPanel = new DashboardPanel();
				CustomerPanel customerPanel = new CustomerPanel();
				AppointmentPanel appointmentPanel = new AppointmentPanel();
				ServicePanel servicePanel = new ServicePanel();
				ReportPanel reportPanel = new ReportPanel();

				// Thêm các panel vào dashboard
				dashboard.addPanel(MainDashboard.PANEL_DASHBOARD, dashboardPanel);
				dashboard.addPanel(MainDashboard.PANEL_CUSTOMER, customerPanel);
				dashboard.addPanel(MainDashboard.PANEL_APPOINTMENT, appointmentPanel);
				dashboard.addPanel(MainDashboard.PANEL_SERVICE, servicePanel);
				dashboard.addPanel(MainDashboard.PANEL_REPORT, reportPanel);

				// Hiển thị dashboard
				dashboard.setVisible(true);
				dashboard.showPanel(MainDashboard.PANEL_DASHBOARD);

			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Lỗi khởi động ứng dụng: " + e.getMessage(),
						"Lỗi khởi động", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}

	private static void configureLightPalette() {
		UIManager.put("Panel.background", new java.awt.Color(248, 250, 252));
		UIManager.put("Table.background", java.awt.Color.WHITE);
		UIManager.put("Table.alternateRowColor", new java.awt.Color(245, 247, 250));
		UIManager.put("Table.selectionBackground", new java.awt.Color(219, 234, 254));
		UIManager.put("Table.selectionForeground", new java.awt.Color(15, 23, 42));
		UIManager.put("TableHeader.background", new java.awt.Color(239, 246, 255));
		UIManager.put("TableHeader.foreground", new java.awt.Color(30, 41, 59));
		UIManager.put("Component.borderColor", new java.awt.Color(203, 213, 225));
		UIManager.put("Button.arc", 8);
		UIManager.put("Component.arc", 8);
	}
}
