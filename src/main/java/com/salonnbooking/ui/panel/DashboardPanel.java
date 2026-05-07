package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.DashboardRequests;
import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.client.ApiClient;

public class DashboardPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final JLabel totalCustomersValue = new JLabel("-");
	private final JLabel todayAppointmentsValue = new JLabel("-");
	private final JLabel pendingAppointmentsValue = new JLabel("-");
	private final JLabel todayRevenueValue = new JLabel("-");
	private final JLabel monthlyRevenueValue = new JLabel("-");
	private final JLabel topServiceValue = new JLabel("-");
	private final JLabel quickStatsValue = new JLabel("-");
	private final JLabel completionRateValue = new JLabel("-");
	private final JLabel statusLabel = new JLabel("Ready");
	private final DefaultTableModel todayTableModel = new DefaultTableModel(
			new String[] { "Time", "Customer", "Service", "Status", "Note" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	public DashboardPanel() {
		setLayout(new BorderLayout(16, 16));
		setBorder(new EmptyBorder(18, 18, 18, 18));
		setBackground(UIManager.getColor("Panel.background"));

		add(createHeader(), BorderLayout.NORTH);
		add(createContent(), BorderLayout.CENTER);

		loadDashboard();
	}

	private JPanel createHeader() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JLabel title = new JLabel("Tổng quan");
		title.setFont(new Font("Segoe UI", Font.BOLD, 24));
		panel.add(title, BorderLayout.WEST);

		JButton refreshButton = new JButton("Làm mới");
		refreshButton.addActionListener(e -> loadDashboard());
		panel.add(refreshButton, BorderLayout.EAST);

		return panel;
	}

	private JPanel createContent() {
		JPanel content = new JPanel(new BorderLayout(16, 16));
		content.setOpaque(false);
		content.add(createStatsGrid(), BorderLayout.NORTH);
		content.add(createTodayTable(), BorderLayout.CENTER);
		content.add(statusLabel, BorderLayout.SOUTH);
		return content;
	}

	private JPanel createStatsGrid() {
		JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
		grid.setOpaque(false);
		grid.add(createStatCard("Khách hàng", totalCustomersValue, new Color(55, 116, 181)));
		grid.add(createStatCard("Hôm nay", todayAppointmentsValue, new Color(44, 145, 120)));
		grid.add(createStatCard("Chờ xử lý", pendingAppointmentsValue, new Color(184, 122, 37)));
		grid.add(createStatCard("Doanh thu hôm nay", todayRevenueValue, new Color(132, 84, 166)));
		grid.add(createStatCard("Doanh thu tháng", monthlyRevenueValue, new Color(41, 128, 185)));
		grid.add(createStatCard("Dịch vụ hàng đầu", topServiceValue, new Color(105, 128, 65)));
		grid.add(createStatCard("Tháng này", quickStatsValue, new Color(152, 89, 72)));
		grid.add(createStatCard("Hoàn thành", completionRateValue, new Color(83, 96, 125)));
		return grid;
	}

	private JPanel createStatCard(String label, JLabel valueLabel, Color accent) {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
				new EmptyBorder(12, 14, 12, 14)));
		card.setBackground(UIManager.getColor("Table.background"));
		card.setPreferredSize(new Dimension(150, 92));

		JLabel labelView = new JLabel(label);
		labelView.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		labelView.setAlignmentX(Component.LEFT_ALIGNMENT);

		valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
		valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		card.add(labelView);
		card.add(Box.createVerticalStrut(8));
		card.add(valueLabel);
		return card;
	}

	private JScrollPane createTodayTable() {
		JTable table = new JTable(todayTableModel);
		table.setRowHeight(28);
		table.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Lịch hẹn sắp tới hôm nay"));
		return scrollPane;
	}

	private void loadDashboard() {
		statusLabel.setText("Loading dashboard...");

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
					statusLabel.setText("Updated");
				} catch (Exception e) {
					statusLabel.setText("Could not load dashboard");
					JOptionPane.showMessageDialog(DashboardPanel.this,
							"Error loading dashboard: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
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
		topServiceValue.setText(dashboard.topServiceName() == null ? "No data" : dashboard.topServiceName());
		quickStatsValue.setText(quickStats.appointmentsThisMonth() + " bookings");
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
						a.status(),
						a.note() == null ? "" : a.note()
				}));

		if (todayTableModel.getRowCount() == 0) {
			todayTableModel.addRow(new Object[] { "", "No appointments today", "", "", "" });
		}
	}

	private String findCustomerName(List<CustomerRequests.Response> customers, Integer id) {
		return customers.stream()
				.filter(c -> c.id().equals(id))
				.map(CustomerRequests.Response::fullName)
				.findFirst()
				.orElse("Unknown");
	}

	private String findServiceName(List<ServiceRequests.Response> services, Integer id) {
		return services.stream()
				.filter(s -> s.id().equals(id))
				.map(ServiceRequests.Response::name)
				.findFirst()
				.orElse("Unknown");
	}

	private String formatCurrency(BigDecimal value) {
		return CURRENCY.format(value == null ? BigDecimal.ZERO : value);
	}

	private String formatPercent(Double value) {
		return String.format(Locale.US, "%.1f%%", value == null ? 0.0 : value);
	}

	private record DashboardData(
			DashboardRequests.DashboardResponse dashboard,
			DashboardRequests.QuickStatsResponse quickStats,
			List<CustomerRequests.Response> customers,
			List<ServiceRequests.Response> services,
			List<AppointmentRequests.Response> appointments) {
	}
}
