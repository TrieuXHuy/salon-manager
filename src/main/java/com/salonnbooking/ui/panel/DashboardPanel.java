package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.DashboardRequests;
import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class DashboardPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final DateTimeFormatter DATE_HEADER_FORMAT =
			DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));

	private static final Color BG_MAIN = new Color(248, 250, 252);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);
	private static final Color BORDER = new Color(226, 232, 240);
	private static final Color PRIMARY = new Color(109, 73, 224);
	private static final Color PRIMARY_SOFT = new Color(237, 233, 255);

	private static final Font TITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 26));
	private static final Font SUBTITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12));
	private static final Font STAT_LABEL_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12));
	private static final Font STAT_VALUE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 20));
	private static final Font TABLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13));

	private final JLabel totalCustomersValue = new JLabel("-");
	private final JLabel todayAppointmentsValue = new JLabel("-");
	private final JLabel pendingAppointmentsValue = new JLabel("-");
	private final JLabel todayRevenueValue = new JLabel("-");
	private final JLabel monthlyRevenueValue = new JLabel("-");
	private final JLabel topServiceValue = new JLabel("-");
	private final JLabel quickStatsValue = new JLabel("-");
	private final JLabel completionRateValue = new JLabel("-");
	private final JLabel statusLabel = new JLabel("Sẵn sàng");
	private final DefaultTableModel todayTableModel = new DefaultTableModel(
			new String[] { "Thời gian", "Khách hàng", "Dịch vụ", "Trạng thái", "Ghi chú" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	public DashboardPanel() {
		setLayout(new MigLayout("insets 24, fill, wrap 1", "[grow]", "[]18[]18[grow]18[]"));
		setBackground(BG_MAIN);

		add(createHeader(), "growx");
		add(createStatsGrid(), "growx");
		add(createTodayTable(), "grow");
		add(createFooter(), "growx");

		loadDashboard();
	}

	private JPanel createHeader() {
		JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[]"));
		header.setOpaque(false);

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel title = new JLabel("Tổng quan");
		title.setFont(TITLE_FONT);
		title.setForeground(TEXT_MAIN);

		JLabel subtitle = new JLabel("Theo dõi hoạt động hôm nay");
		subtitle.setFont(SUBTITLE_FONT);
		subtitle.setForeground(TEXT_MUTED);

		titleBlock.add(title);
		titleBlock.add(Box.createVerticalStrut(4));
		titleBlock.add(subtitle);

		header.add(titleBlock, "growx");
		header.add(createHeaderActions(), "aligny top");

		return header;
	}

	private JPanel createHeaderActions() {
		JPanel actions = new JPanel(new MigLayout("insets 0, gap 8", "[]8[]", "[]"));
		actions.setOpaque(false);

		String dateText = capitalize(LocalDate.now().format(DATE_HEADER_FORMAT));
		RoundedPanel datePill = new RoundedPanel(14, BG_CARD, true);
		datePill.setLayout(new MigLayout("insets 6 12, fill", "[]", "[]"));
		JLabel dateLabel = new JLabel(dateText);
		dateLabel.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		dateLabel.setForeground(TEXT_MAIN);
		datePill.add(dateLabel);

		JButton refreshButton = new JButton("Làm mới");
		refreshButton.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		refreshButton.setForeground(Color.WHITE);
		refreshButton.setBackground(PRIMARY);
		refreshButton.setFocusPainted(false);
		refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		refreshButton.setBorder(new EmptyBorder(8, 16, 8, 16));
		refreshButton.addActionListener(e -> loadDashboard());

		actions.add(datePill);
		actions.add(refreshButton);

		return actions;
	}

	private JPanel createStatsGrid() {
		JPanel grid = new JPanel(new MigLayout("insets 0, gap 16, wrap 4", "[grow, fill][grow, fill][grow, fill][grow, fill]", "[]"));
		grid.setOpaque(false);
		grid.add(createStatCard("Khách hàng", totalCustomersValue, "Tổng số khách hàng", new Color(124, 92, 255), "KH"));
		grid.add(createStatCard("Hôm nay", todayAppointmentsValue, "Lịch hẹn hôm nay", new Color(34, 197, 94), "HN"));
		grid.add(createStatCard("Chờ xử lý", pendingAppointmentsValue, "Lịch hẹn chờ xử lý", new Color(245, 158, 11), "CH"));
		grid.add(createStatCard("Doanh thu hôm nay", todayRevenueValue, "Tổng doanh thu", new Color(99, 102, 241), "DT"));
		grid.add(createStatCard("Doanh thu tháng", monthlyRevenueValue, "Tổng doanh thu tháng", new Color(59, 130, 246), "TM"));
		grid.add(createStatCard("Dịch vụ hàng đầu", topServiceValue, "Dịch vụ được đặt nhiều nhất", new Color(16, 185, 129), "DV"));
		grid.add(createStatCard("Tháng này", quickStatsValue, "Tổng số lịch hẹn", new Color(244, 114, 182), "TH"));
		grid.add(createStatCard("Hoàn thành", completionRateValue, "Tỷ lệ hoàn thành", new Color(94, 108, 132), "HT"));
		return grid;
	}

	private RoundedPanel createStatCard(String label, JLabel valueLabel, String subtitle, Color accent, String badgeText) {
		RoundedPanel card = new RoundedPanel(16, BG_CARD, true);
		card.setLayout(new MigLayout("insets 14, fill", "[]12[grow]", "[]4[]4[]"));
		card.setPreferredSize(new Dimension(170, 110));

		CircleBadge badge = new CircleBadge(badgeText, accent);

		JLabel labelView = new JLabel(label);
		labelView.setFont(STAT_LABEL_FONT);
		labelView.setForeground(TEXT_MUTED);

		valueLabel.setFont(STAT_VALUE_FONT);
		valueLabel.setForeground(TEXT_MAIN);

		JLabel subtitleLabel = new JLabel(subtitle);
		subtitleLabel.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 11)));
		subtitleLabel.setForeground(TEXT_MUTED);

		card.add(badge, "spany 3, aligny top");
		card.add(labelView, "wrap");
		card.add(valueLabel, "wrap");
		card.add(subtitleLabel);

		return card;
	}

	private JPanel createTodayTable() {
		RoundedPanel container = new RoundedPanel(18, BG_CARD, true);
		container.setLayout(new MigLayout("insets 18, fill", "[grow]", "[]12[grow]"));

		JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[]"));
		header.setOpaque(false);

		JLabel title = new JLabel("Lịch hẹn sắp tới hôm nay");
		title.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 16)));
		title.setForeground(TEXT_MAIN);
		header.add(title, "growx");

		JButton viewAll = new JButton("Xem tất cả ->");
		viewAll.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		viewAll.setForeground(PRIMARY);
		viewAll.setBackground(PRIMARY_SOFT);
		viewAll.setFocusPainted(false);
		viewAll.setBorder(new EmptyBorder(6, 12, 6, 12));
		header.add(viewAll);

		container.add(header, "wrap");

		JTable table = new JTable(todayTableModel);
		table.setRowHeight(40);
		table.setFont(TABLE_FONT);
		table.setForeground(TEXT_MAIN);
		table.setGridColor(BORDER);
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setFillsViewportHeight(true);
		table.setBackground(Color.WHITE);
		table.setSelectionBackground(new Color(241, 245, 249));
		table.setSelectionForeground(TEXT_MAIN);

		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		tableHeader.setBackground(new Color(248, 250, 252));
		tableHeader.setForeground(TEXT_MUTED);
		tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
		tableHeader.setPreferredSize(new Dimension(100, 40));
		tableHeader.setReorderingAllowed(false);
		((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

		// Custom Header Renderer for padding
		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
			@Override
			public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
				setBackground(new Color(248, 250, 252));
				setForeground(TEXT_MUTED);
				setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
				return this;
			}
		};
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		DefaultTableCellRenderer baseRenderer = createBaseRenderer();
		table.setDefaultRenderer(Object.class, baseRenderer);
		table.getColumnModel().getColumn(0).setCellRenderer(createCenteredRenderer());
		table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(180);
		table.getColumnModel().getColumn(2).setPreferredWidth(180);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(4).setPreferredWidth(220);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(BG_CARD);

		container.add(scrollPane, "grow");
		return container;
	}

	private JPanel createFooter() {
		JPanel footer = new JPanel(new BorderLayout());
		footer.setOpaque(false);
		statusLabel.setFont(SUBTITLE_FONT);
		statusLabel.setForeground(TEXT_MUTED);

		JLabel note = new JLabel("Salon Pro - Quản lý salon chuyên nghiệp", SwingConstants.CENTER);
		note.setFont(SUBTITLE_FONT);
		note.setForeground(TEXT_MUTED);

		footer.add(statusLabel, BorderLayout.WEST);
		footer.add(note, BorderLayout.CENTER);
		return footer;
	}

	private void loadDashboard() {
		statusLabel.setText("Đang tải tổng quan...");

		SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
			@Override
			protected DashboardData doInBackground() throws Exception {
				DashboardRequests.DashboardResponse dashboard = ApiClient.getDashboard();
				DashboardRequests.QuickStatsResponse quickStats = ApiClient.getQuickStats();
				List<CustomerRequests.Response> customers = ApiClient.getAllCustomers();
				List<ServiceRequests.Response> services = ApiClient.getAllServices();
				List<AppointmentRequests.Response> appointments = ApiClient.getAllAppointments();
				return new DashboardData(dashboard, quickStats, customers, services, appointments);
			}

			@Override
			protected void done() {
				try {
					applyData(get());
					statusLabel.setText("Đã cập nhật");
				} catch (Exception e) {
					statusLabel.setText("Không tải được tổng quan");
					JOptionPane.showMessageDialog(DashboardPanel.this,
							"Lỗi tải tổng quan: " + e.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private void applyData(DashboardData data) {
		DashboardRequests.DashboardResponse dashboard = data.dashboard();
		DashboardRequests.QuickStatsResponse quickStats = data.quickStats();

		totalCustomersValue.setText(String.valueOf(dashboard.totalCustomers()));
		todayAppointmentsValue.setText(String.valueOf(dashboard.totalAppointmentsToday()));
		pendingAppointmentsValue.setText(String.valueOf(dashboard.pendingAppointments()));
		todayRevenueValue.setText(formatCurrency(dashboard.todayRevenue()));
		monthlyRevenueValue.setText(formatCurrency(dashboard.monthlyRevenue()));
		topServiceValue.setText(dashboard.topServiceName() == null ? "Chưa có dữ liệu" : dashboard.topServiceName());
		quickStatsValue.setText(quickStats.appointmentsThisMonth() + " lịch hẹn");
		completionRateValue.setText(formatPercent(dashboard.appointmentCompletionRate()));

		todayTableModel.setRowCount(0);
		LocalDate today = LocalDate.now();
		data.appointments().stream()
				.filter(a -> a.appointmentTime().toLocalDate().equals(today))
				.sorted((a, b) -> a.appointmentTime().compareTo(b.appointmentTime()))
				.forEach(a -> todayTableModel.addRow(new Object[] {
						a.appointmentTime().format(DATE_TIME_FORMAT),
						findCustomerName(data.customers(), a.customerId()),
						findServiceName(data.services(), a.serviceId()),
						formatStatus(a.status().toString()),
						a.note() == null ? "" : a.note()
				}));

		if (todayTableModel.getRowCount() == 0) {
			todayTableModel.addRow(new Object[] { "", "Hôm nay chưa có lịch hẹn", "", "", "" });
		}
	}

	private String findCustomerName(List<CustomerRequests.Response> customers, Integer id) {
		return customers.stream()
				.filter(c -> c.id().equals(id))
				.map(CustomerRequests.Response::fullName)
				.findFirst()
				.orElse("Không rõ");
	}

	private String findServiceName(List<ServiceRequests.Response> services, Integer id) {
		return services.stream()
				.filter(s -> s.id().equals(id))
				.map(ServiceRequests.Response::name)
				.findFirst()
				.orElse("Không rõ");
	}

	private String formatCurrency(BigDecimal value) {
		return CURRENCY.format(value == null ? BigDecimal.ZERO : value);
	}

	private String formatPercent(Double value) {
		return String.format(Locale.US, "%.1f%%", value == null ? 0.0 : value);
	}

	private String formatStatus(String status) {
		if (status == null || status.isBlank()) {
			return "-";
		}
		String normalized = status.trim().toLowerCase(Locale.US);
		return switch (normalized) {
			case "confirmed" -> "Đã xác nhận";
			case "pending" -> "Chờ xử lý";
			case "completed" -> "Hoàn thành";
			case "cancelled" -> "Đã hủy";
			default -> capitalize(status);
		};
	}

	private DefaultTableCellRenderer createBaseRenderer() {
		return new DefaultTableCellRenderer() {
			@Override
			public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				label.setFont(TABLE_FONT);
				label.setBorder(new EmptyBorder(0, 16, 0, 16));
				label.setForeground(TEXT_MAIN);
				if (!isSelected) {
					label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
				} else {
					label.setBackground(table.getSelectionBackground());
				}
				return label;
			}
		};
	}

	private DefaultTableCellRenderer createCenteredRenderer() {
		DefaultTableCellRenderer renderer = createBaseRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		return renderer;
	}

	private String capitalize(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return value.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + value.substring(1);
	}

	private record DashboardData(
			DashboardRequests.DashboardResponse dashboard,
			DashboardRequests.QuickStatsResponse quickStats,
			List<CustomerRequests.Response> customers,
			List<ServiceRequests.Response> services,
			List<AppointmentRequests.Response> appointments) {
	}

	private static final class CircleBadge extends JLabel {
		private static final long serialVersionUID = 1L;
		private final Color accent;

		private CircleBadge(String text, Color accent) {
			super(text, SwingConstants.CENTER);
			this.accent = accent;
			setPreferredSize(new Dimension(36, 36));
			setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
			setForeground(accent.darker());
		}

		@Override
		protected void paintComponent(java.awt.Graphics g) {
			java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
			g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
					java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
			g2.fillOval(0, 0, getWidth(), getHeight());
			g2.dispose();
			super.paintComponent(g);
		}
	}

	private static final class StatusCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			String status = value == null ? "" : value.toString().toLowerCase(Locale.US);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBorder(new EmptyBorder(6, 8, 6, 8));
			label.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 11)));

			if (status.contains("xác nhận")) {
				styleStatus(label, new Color(220, 252, 231), new Color(22, 163, 74));
			} else if (status.contains("chờ") || status.contains("pending")) {
				styleStatus(label, new Color(254, 243, 199), new Color(217, 119, 6));
			} else if (status.contains("hoàn thành")) {
				styleStatus(label, new Color(219, 234, 254), new Color(37, 99, 235));
			} else if (status.contains("hủy") || status.contains("cancel")) {
				styleStatus(label, new Color(254, 226, 226), new Color(220, 38, 38));
			} else {
				styleStatus(label, new Color(241, 245, 249), new Color(100, 116, 139));
			}

			return label;
		}

		private void styleStatus(JLabel label, Color bg, Color fg) {
			label.setBackground(bg);
			label.setForeground(fg);
			label.setOpaque(true);
		}
	}
}
