package com.salonnbooking;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.ui.LoginFrame;
import com.salonnbooking.ui.MainDashboard;
import com.salonnbooking.ui.panel.AppointmentPanel;
import com.salonnbooking.ui.panel.CustomerPanel;
import com.salonnbooking.ui.panel.DashboardPanel;
import com.salonnbooking.ui.panel.ReportPanel;
import com.salonnbooking.ui.panel.ServicePanel;

public class SwingClient {

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		System.setProperty("sun.jnu.encoding", "UTF-8");

		FlatLightLaf.setup();
		configureLightPalette();

		SwingUtilities.invokeLater(() -> {
			try {
				showLogin();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Cannot start application: " + e.getMessage(),
						"Startup error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}

	private static void showLogin() {
		new LoginFrame(response -> showDashboard(response.username(), response.role())).setVisible(true);
	}

	private static void showDashboard(String username, com.salonnbooking.domain.UserRole role) {
		final MainDashboard[] dashboardRef = new MainDashboard[1];
		MainDashboard dashboard = new MainDashboard(username, role, () -> {
			try {
				ApiClient.logout();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			dashboardRef[0].dispose();
			showLogin();
		});
		dashboardRef[0] = dashboard;

		dashboard.addPanel(MainDashboard.PANEL_DASHBOARD, new DashboardPanel());
		dashboard.addPanel(MainDashboard.PANEL_CUSTOMER, new CustomerPanel());
		dashboard.addPanel(MainDashboard.PANEL_APPOINTMENT, new AppointmentPanel());
		dashboard.addPanel(MainDashboard.PANEL_SERVICE, new ServicePanel());
		dashboard.addPanel(MainDashboard.PANEL_REPORT, new ReportPanel());

		dashboard.setVisible(true);
		dashboard.showPanel(MainDashboard.PANEL_DASHBOARD);
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
