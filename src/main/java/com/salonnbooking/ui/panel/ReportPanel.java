package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.salonnbooking.api.dto.ReportRequests;
import com.salonnbooking.client.ApiClient;

public class ReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

	private final JTextField startDateField = new JTextField(LocalDate.now().minusDays(30).toString(), 10);
	private final JTextField endDateField = new JTextField(LocalDate.now().toString(), 10);
	private final JLabel statusLabel = new JLabel("Ready");
	private final JLabel totalAppointmentsValue = new JLabel("-");
	private final JLabel pendingValue = new JLabel("-");
	private final JLabel confirmedValue = new JLabel("-");
	private final JLabel completedValue = new JLabel("-");
	private final JLabel cancelledValue = new JLabel("-");

	private final DefaultTableModel dailyRevenueModel = readOnlyModel(
			"Date", "Revenue", "Appointments", "Completed");
	private final DefaultTableModel serviceRevenueModel = readOnlyModel(
			"Service", "Appointments", "Total revenue", "Average revenue");
	private final DefaultTableModel paymentMethodModel = readOnlyModel(
			"Payment method", "Count", "Total amount", "Percentage");

	public ReportPanel() {
		setLayout(new BorderLayout(16, 16));
		setBorder(new EmptyBorder(18, 18, 18, 18));
		setBackground(UIManager.getColor("Panel.background"));

		add(createHeader(), BorderLayout.NORTH);
		add(createContent(), BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		loadReports();
	}

	private JPanel createHeader() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JLabel title = new JLabel("Báo cáo");
		title.setFont(new Font("Segoe UI", Font.BOLD, 24));
		panel.add(title, BorderLayout.WEST);

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		controls.setOpaque(false);
		controls.add(new JLabel("Từ ngày"));
		controls.add(startDateField);
		controls.add(new JLabel("Đến ngày"));
		controls.add(endDateField);

		JButton refreshButton = new JButton("Làm mới");
		refreshButton.addActionListener(e -> loadReports());
		controls.add(refreshButton);
		panel.add(controls, BorderLayout.EAST);

		return panel;
	}

	private JPanel createContent() {
		JPanel content = new JPanel(new BorderLayout(12, 12));
		content.setOpaque(false);
		content.add(createStatsPanel(), BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Daily revenue", createTable(dailyRevenueModel));
		tabs.addTab("Service revenue", createTable(serviceRevenueModel));
		tabs.addTab("Payment methods", createTable(paymentMethodModel));
		content.add(tabs, BorderLayout.CENTER);

		return content;
	}

	private JPanel createStatsPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 5, 10, 0));
		panel.setOpaque(false);
		panel.add(createStat("Total", totalAppointmentsValue));
		panel.add(createStat("Pending", pendingValue));
		panel.add(createStat("Confirmed", confirmedValue));
		panel.add(createStat("Completed", completedValue));
		panel.add(createStat("Cancelled", cancelledValue));
		return panel;
	}

	private JPanel createStat(String label, JLabel value) {
		JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
				new EmptyBorder(10, 12, 10, 12)));
		panel.setBackground(UIManager.getColor("Table.background"));

		JLabel labelView = new JLabel(label);
		labelView.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		value.setFont(new Font("Segoe UI", Font.BOLD, 20));

		panel.add(labelView, BorderLayout.NORTH);
		panel.add(value, BorderLayout.CENTER);
		return panel;
	}

	private JScrollPane createTable(DefaultTableModel model) {
		JTable table = new JTable(model);
		table.setRowHeight(28);
		table.getTableHeader().setReorderingAllowed(false);
		return new JScrollPane(table);
	}

	private static DefaultTableModel readOnlyModel(String... columns) {
		return new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
	}

	private void loadReports() {
		LocalDate startDate;
		LocalDate endDate;
		try {
			startDate = LocalDate.parse(startDateField.getText().trim());
			endDate = LocalDate.parse(endDateField.getText().trim());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Dates must use yyyy-MM-dd format.",
					"Invalid Date", JOptionPane.WARNING_MESSAGE);
			return;
		}

		statusLabel.setText("Loading reports...");
		SwingWorker<ReportData, Void> worker = new SwingWorker<>() {
			@Override
			protected ReportData doInBackground() throws Exception {
				return new ReportData(
						ApiClient.getAppointmentStats(),
						ApiClient.getDailyRevenueReport(startDate, endDate),
						ApiClient.getServiceRevenueReport(),
						ApiClient.getPaymentMethodReport());
			}

			@Override
			protected void done() {
				try {
					applyData(get());
					statusLabel.setText("Updated");
				} catch (Exception e) {
					statusLabel.setText("Could not load reports");
					JOptionPane.showMessageDialog(ReportPanel.this,
							"Error loading reports: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.execute();
	}

	private void applyData(ReportData data) {
		ReportRequests.AppointmentStatsResponse stats = data.stats();
		totalAppointmentsValue.setText(String.valueOf(stats.totalAppointments()));
		pendingValue.setText(String.valueOf(stats.pendingAppointments()));
		confirmedValue.setText(String.valueOf(stats.confirmedAppointments()));
		completedValue.setText(String.valueOf(stats.completedAppointments()));
		cancelledValue.setText(String.valueOf(stats.cancelledAppointments()));

		dailyRevenueModel.setRowCount(0);
		for (ReportRequests.DailyRevenueResponse row : data.dailyRevenue()) {
			dailyRevenueModel.addRow(new Object[] {
					row.date(),
					formatCurrency(row.totalRevenue()),
					row.appointmentCount(),
					row.completedCount()
			});
		}

		serviceRevenueModel.setRowCount(0);
		for (ReportRequests.ServiceRevenueResponse row : data.serviceRevenue()) {
			serviceRevenueModel.addRow(new Object[] {
					row.serviceName(),
					row.appointmentCount(),
					formatCurrency(row.totalRevenue()),
					formatCurrency(row.avgRevenue())
			});
		}

		paymentMethodModel.setRowCount(0);
		for (ReportRequests.PaymentMethodResponse row : data.paymentMethods()) {
			paymentMethodModel.addRow(new Object[] {
					row.paymentMethod(),
					row.count(),
					formatCurrency(row.totalAmount()),
					String.format(Locale.US, "%.1f%%", row.percentage())
			});
		}
	}

	private String formatCurrency(BigDecimal value) {
		return CURRENCY.format(value == null ? BigDecimal.ZERO : value);
	}

	private record ReportData(
			ReportRequests.AppointmentStatsResponse stats,
			List<ReportRequests.DailyRevenueResponse> dailyRevenue,
			List<ReportRequests.ServiceRevenueResponse> serviceRevenue,
			List<ReportRequests.PaymentMethodResponse> paymentMethods) {
	}
}
