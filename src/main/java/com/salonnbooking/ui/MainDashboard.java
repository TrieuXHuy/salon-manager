package com.salonnbooking.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatLightLaf;

/**
 * MainDashboard - Khung sườn chính của ứng dụng Salon Booking
 * 
 * Bố cục:
 * - BorderLayout.WEST: Sidebar điều hướng
 * - BorderLayout.CENTER: Content Panel với CardLayout
 */
public class MainDashboard extends JFrame {
	private static final long serialVersionUID = 1L;
	private CardLayout cardLayout;
	private JPanel contentPanel;
	private JButton[] navButtons;

	// Constants cho panel names
	public static final String PANEL_DASHBOARD = "dashboard";
	public static final String PANEL_CUSTOMER = "customer";
	public static final String PANEL_APPOINTMENT = "appointment";
	public static final String PANEL_SERVICE = "service";
	public static final String PANEL_REPORT = "report";

	public MainDashboard() {
		// Setup FlatLaf Theme
		configureLightPalette();

		setTitle("Hệ thống đặt lịch salon");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 700);
		setLocationRelativeTo(null);
		setResizable(true);

		// Main Container với BorderLayout
		JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
		mainContainer.setBackground(UIManager.getColor("Panel.background"));

		// ==================== SIDEBAR ====================
		JPanel sidebar = createSidebar();
		mainContainer.add(sidebar, BorderLayout.WEST);

		// ==================== CONTENT AREA (CardLayout) ====================
		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);
		contentPanel.setBackground(UIManager.getColor("Panel.background"));

		// Placeholder panels (sẽ được thay thế bằng các Panel thực tế)
		contentPanel.add(createPlaceholderPanel("Tổng quan", PANEL_DASHBOARD), PANEL_DASHBOARD);
		contentPanel.add(createPlaceholderPanel("Quản lý khách hàng", PANEL_CUSTOMER), PANEL_CUSTOMER);
		contentPanel.add(createPlaceholderPanel("Đặt lịch hẹn", PANEL_APPOINTMENT), PANEL_APPOINTMENT);
		contentPanel.add(createPlaceholderPanel("Quản lý dịch vụ", PANEL_SERVICE), PANEL_SERVICE);
		contentPanel.add(createPlaceholderPanel("Báo cáo", PANEL_REPORT), PANEL_REPORT);

		mainContainer.add(contentPanel, BorderLayout.CENTER);

		setContentPane(mainContainer);
	}

	/**
	 * Tạo Sidebar với các nút điều hướng
	 */
	private JPanel createSidebar() {
		JPanel sidebar = new JPanel();
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		sidebar.setPreferredSize(new Dimension(200, 0));
		sidebar.setBackground(UIManager.getColor("Panel.background"));
		sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

		// Header
		JLabel headerLabel = new JLabel("Đặt lịch salon");
		headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
		headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sidebar.add(headerLabel);

		sidebar.add(Box.createVerticalStrut(20));

		// Navigation Buttons
		String[] buttonLabels = { "Tổng quan", "Khách hàng", "Lịch hẹn", "Dịch vụ", "Báo cáo" };
		String[] panelNames = { PANEL_DASHBOARD, PANEL_CUSTOMER, PANEL_APPOINTMENT, PANEL_SERVICE,
				PANEL_REPORT };

		navButtons = new JButton[buttonLabels.length];

		for (int i = 0; i < buttonLabels.length; i++) {
			navButtons[i] = createNavButton(buttonLabels[i], panelNames[i]);
			sidebar.add(navButtons[i]);
			sidebar.add(Box.createVerticalStrut(10));
		}

		sidebar.add(Box.createVerticalGlue());

		// Logout Button
		JButton logoutBtn = new JButton("Đăng xuất");
		logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		logoutBtn.addActionListener(e -> System.exit(0));
		sidebar.add(logoutBtn);

		return sidebar;
	}

	/**
	 * Tạo một nút điều hướng
	 */
	private JButton createNavButton(String label, String panelName) {
		JButton btn = new JButton(label);
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
		btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

		btn.addActionListener((ActionEvent e) -> {
			showPanel(panelName);
		});

		return btn;
	}

	/**
	 * Tạo panel placeholder tạm thời
	 */
	private JPanel createPlaceholderPanel(String title, String panelName) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setName(panelName);
		panel.setBackground(UIManager.getColor("Panel.background"));

		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setFont(new Font("Segoe UI", Font.BOLD, 24));
		panel.add(label, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Thay thế panel tạm thời bằng panel thực tế
	 */
	public void addPanel(String panelName, JPanel panel) {
		panel.setName(panelName);
		Component[] components = contentPanel.getComponents();
		for (Component comp : components) {
			if (panelName.equals(comp.getName())) {
				contentPanel.remove(comp);
				break;
			}
		}
		contentPanel.add(panel, panelName);
		contentPanel.revalidate();
		contentPanel.repaint();
	}

	/**
	 * Hiển thị panel theo tên
	 */
	public void showPanel(String panelName) {
		cardLayout.show(contentPanel, panelName);
		updateNavSelection(panelName);
	}

	private void updateNavSelection(String panelName) {
		String[] panelNames = { PANEL_DASHBOARD, PANEL_CUSTOMER, PANEL_APPOINTMENT, PANEL_SERVICE, PANEL_REPORT };
		if (navButtons == null) {
			return;
		}
		for (int i = 0; i < navButtons.length; i++) {
			navButtons[i].putClientProperty("JButton.buttonType", panelNames[i].equals(panelName) ? "default" : null);
		}
	}

	private static void configureLightPalette() {
		UIManager.put("Panel.background", new Color(248, 250, 252));
		UIManager.put("Table.background", Color.WHITE);
		UIManager.put("Table.alternateRowColor", new Color(245, 247, 250));
		UIManager.put("Table.selectionBackground", new Color(219, 234, 254));
		UIManager.put("Table.selectionForeground", new Color(15, 23, 42));
		UIManager.put("TableHeader.background", new Color(239, 246, 255));
		UIManager.put("TableHeader.foreground", new Color(30, 41, 59));
		UIManager.put("Component.borderColor", new Color(203, 213, 225));
		UIManager.put("Button.arc", 8);
		UIManager.put("Component.arc", 8);
	}

	/**
	 * Main method
	 */
	public static void main(String[] args) {
		FlatLightLaf.setup();
		configureLightPalette();
		SwingUtilities.invokeLater(() -> {
			MainDashboard frame = new MainDashboard();
			frame.setVisible(true);
		});
	}
}
