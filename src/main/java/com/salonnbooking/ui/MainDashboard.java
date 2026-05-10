package com.salonnbooking.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatLightLaf;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.components.SidebarButton;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

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
	private SidebarButton[] navButtons;

	private static final Color BG_MAIN = new Color(248, 250, 252);
	private static final Color BG_SIDEBAR = Color.WHITE;
	private static final Color PRIMARY = new Color(109, 73, 224);
	private static final Color PRIMARY_SOFT = new Color(237, 233, 255);
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);
	private static final Color BORDER = new Color(226, 232, 240);

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
		JPanel mainContainer = new GradientPanel();
		mainContainer.setLayout(new BorderLayout(16, 16));

		// ==================== SIDEBAR ====================
		JPanel sidebar = createSidebar();
		mainContainer.add(sidebar, BorderLayout.WEST);

		// ==================== CONTENT AREA (CardLayout) ====================
		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);
		contentPanel.setBackground(BG_MAIN);

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
		RoundedPanel sidebar = new RoundedPanel(18, BG_SIDEBAR, true);
		sidebar.setLayout(new MigLayout("insets 18 14 14 14, fillx, wrap 1",
				"[grow]", "[]12[]12[grow]12[]12[]"));
		sidebar.setPreferredSize(new Dimension(230, 0));

		sidebar.add(createBrandBlock(), "growx");

		JPanel menuPanel = new JPanel();
		menuPanel.setOpaque(false);
		menuPanel.setLayout(new MigLayout("insets 0, wrap 1, gap 6", "[grow]", "[]"));
		String[] buttonLabels = { "Tổng quan", "Khách hàng", "Lịch hẹn", "Dịch vụ", "Báo cáo" };
		String[] buttonTags = { "TG", "KH", "LH", "DV", "BC" };
		String[] panelNames = { PANEL_DASHBOARD, PANEL_CUSTOMER, PANEL_APPOINTMENT, PANEL_SERVICE, PANEL_REPORT };

		navButtons = new SidebarButton[buttonLabels.length];
		for (int i = 0; i < buttonLabels.length; i++) {
			SidebarButton btn = createNavButton(buttonTags[i] + "  " + buttonLabels[i], panelNames[i]);
			navButtons[i] = btn;
			menuPanel.add(btn, "growx");
		}

		sidebar.add(menuPanel, "growx");
		sidebar.add(createLogoutButton(), "growx");
		sidebar.add(createUserCard(), "growx");

		return sidebar;
	}

	private JComponent createBrandBlock() {
		JPanel brand = new JPanel();
		brand.setOpaque(false);
		brand.setLayout(new MigLayout("insets 0, fillx", "[]10[grow]", "[]"));

		CircleBadge badge = new CircleBadge("SP", PRIMARY);
		brand.add(badge, "aligny top");

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel title = new JLabel("Salon Pro");
		title.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 18)));
		title.setForeground(PRIMARY);

		JLabel subtitle = new JLabel("Đặt lịch dễ dàng");
		subtitle.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		subtitle.setForeground(TEXT_MUTED);

		titleBlock.add(title);
		titleBlock.add(Box.createVerticalStrut(4));
		titleBlock.add(subtitle);
		brand.add(titleBlock, "growx");

		return brand;
	}

	/**
	 * Tạo một nút điều hướng
	 */
	private SidebarButton createNavButton(String label, String panelName) {
		SidebarButton btn = new SidebarButton(label);
		btn.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13)));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.addActionListener((ActionEvent e) -> showPanel(panelName));
		return btn;
	}

	private JButton createLogoutButton() {
		JButton logoutBtn = new JButton("Đăng xuất");
		logoutBtn.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		logoutBtn.setForeground(PRIMARY);
		logoutBtn.setBackground(PRIMARY_SOFT);
		logoutBtn.setFocusPainted(false);
		logoutBtn.setBorder(new EmptyBorder(10, 14, 10, 14));
		logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		logoutBtn.addActionListener(e -> System.exit(0));
		return logoutBtn;
	}

	private JComponent createUserCard() {
		RoundedPanel card = new RoundedPanel(14, new Color(249, 250, 251), true);
		card.setLayout(new MigLayout("insets 10, fillx", "[]10[grow]", "[]"));
		card.setBorder(BorderFactory.createLineBorder(BORDER));

		CircleBadge avatar = new CircleBadge("A", new Color(148, 163, 184));
		card.add(avatar);

		JPanel info = new JPanel();
		info.setOpaque(false);
		info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

		JLabel name = new JLabel("Admin");
		name.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		name.setForeground(TEXT_MAIN);

		JLabel role = new JLabel("Quản trị viên");
		role.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 11)));
		role.setForeground(TEXT_MUTED);

		JLabel status = new JLabel("Online");
		status.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 11)));
		status.setForeground(new Color(34, 197, 94));

		info.add(name);
		info.add(Box.createVerticalStrut(2));
		info.add(role);
		info.add(Box.createVerticalStrut(4));
		info.add(status);

		card.add(info, "growx");
		return card;
	}

	/**
	 * Tạo panel placeholder tạm thời
	 */
	private JPanel createPlaceholderPanel(String title, String panelName) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setName(panelName);
		panel.setBackground(BG_MAIN);

		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 24)));
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
			navButtons[i].setActive(panelNames[i].equals(panelName));
		}
	}

	private static void configureLightPalette() {
		UIManager.put("Panel.background", BG_MAIN);
		UIManager.put("Table.background", Color.WHITE);
		UIManager.put("Table.alternateRowColor", new Color(245, 247, 250));
		UIManager.put("Table.selectionBackground", new Color(219, 234, 254));
		UIManager.put("Table.selectionForeground", new Color(15, 23, 42));
		UIManager.put("TableHeader.background", new Color(239, 246, 255));
		UIManager.put("TableHeader.foreground", new Color(30, 41, 59));
		UIManager.put("Component.borderColor", new Color(203, 213, 225));
		UIManager.put("Button.arc", 12);
		UIManager.put("Component.arc", 12);
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

	private static final class GradientPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			GradientPaint paint = new GradientPaint(0, 0, BG_MAIN, getWidth(), getHeight(), new Color(238, 241, 255));
			g2.setPaint(paint);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.dispose();
		}
	}

	private static final class CircleBadge extends JPanel {
		private static final long serialVersionUID = 1L;
		private final String text;
		private final Color color;

		private CircleBadge(String text, Color color) {
			this.text = text;
			this.color = color;
			setPreferredSize(new Dimension(38, 38));
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
			g2.fillOval(0, 0, getWidth(), getHeight());
			g2.setColor(color);
			g2.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
			int textWidth = g2.getFontMetrics().stringWidth(text);
			int textHeight = g2.getFontMetrics().getAscent();
			int x = (getWidth() - textWidth) / 2;
			int y = (getHeight() + textHeight) / 2 - 2;
			g2.drawString(text, x, y);
			g2.dispose();
		}
	}
}
