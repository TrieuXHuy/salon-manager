package com.salonnbooking;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import com.formdev.flatlaf.FlatLightLaf;
import com.salonnbooking.domain.UserAccount;
import com.salonnbooking.ui.MainDashboard;
import com.salonnbooking.ui.auth.LoginFrame;
import com.salonnbooking.ui.panel.AppointmentPanel;
import com.salonnbooking.ui.panel.CustomerPanel;
import com.salonnbooking.ui.panel.DashboardPanel;
import com.salonnbooking.ui.panel.ReportPanel;
import com.salonnbooking.ui.panel.ServicePanel;

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
	private static final int DEFAULT_BACKEND_PORT = 8080;

	private static ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		System.setProperty("sun.jnu.encoding", "UTF-8");

		try {
			SpringApplicationBuilder builder = new SpringApplicationBuilder(SalonnBookingApplication.class)
					.headless(false);

			if (isBackendReachable()) {
				builder.web(WebApplicationType.NONE);
			}

			applicationContext = builder.run(args);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Không khởi động được backend: " + e.getMessage(),
					"Lỗi khởi động", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		FlatLightLaf.setup();
		configureLightPalette();

		SwingUtilities.invokeLater(() -> {
			try {
				showLoginWindow();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Lỗi khởi động ứng dụng: " + e.getMessage(),
						"Lỗi khởi động", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}

	public static void showDashboard(UserAccount userAccount) {
		MainDashboard dashboard = new MainDashboard(userAccount, SwingClient::showLoginWindow);

		DashboardPanel dashboardPanel = new DashboardPanel();
		CustomerPanel customerPanel = new CustomerPanel();
		AppointmentPanel appointmentPanel = new AppointmentPanel();
		ServicePanel servicePanel = new ServicePanel();
		ReportPanel reportPanel = new ReportPanel();

		dashboard.addPanel(MainDashboard.PANEL_DASHBOARD, dashboardPanel);
		dashboard.addPanel(MainDashboard.PANEL_CUSTOMER, customerPanel);
		dashboard.addPanel(MainDashboard.PANEL_APPOINTMENT, appointmentPanel);
		dashboard.addPanel(MainDashboard.PANEL_SERVICE, servicePanel);
		dashboard.addPanel(MainDashboard.PANEL_REPORT, reportPanel);

		dashboard.setVisible(true);
		dashboard.showPanel(MainDashboard.PANEL_DASHBOARD);
	}

	private static void showLoginWindow() {
		LoginFrame loginFrame = new LoginFrame(applicationContext);
		loginFrame.setVisible(true);
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

	private static boolean isBackendReachable() {
		try (java.net.Socket socket = new java.net.Socket()) {
			socket.connect(new java.net.InetSocketAddress("127.0.0.1", DEFAULT_BACKEND_PORT), 1000);
			return true;
		} catch (java.io.IOException ex) {
			return false;
		}
	}
}
