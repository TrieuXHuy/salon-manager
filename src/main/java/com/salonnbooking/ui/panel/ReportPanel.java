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
	private final JLabel statusLabel = new JLabel("Sẵn sàng");
	private final JLabel totalAppointmentsValue = new JLabel("-");
	private final JLabel pendingValue = new JLabel("-");
	private final JLabel confirmedValue = new JLabel("-");
	private final JLabel completedValue = new JLabel("-");
	private final JLabel cancelledValue = new JLabel("-");

	private final DefaultTableModel dailyRevenueModel = readOnlyModel(
			"Ngày", "Doanh thu", "Lịch hẹn", "Hoàn thành");
	private final DefaultTableModel serviceRevenueModel = readOnlyModel(
			"Dịch vụ", "Lịch hẹn", "Tổng doanh thu", "Doanh thu trung bình");
	private final DefaultTableModel paymentMethodModel = readOnlyModel(
			"Phương thức thanh toán", "Số lượng", "Tổng tiền", "Tỷ lệ");

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
		tabs.addTab("Doanh thu ngày", createTable(dailyRevenueModel));
		tabs.addTab("Doanh thu dịch vụ", createTable(serviceRevenueModel));
		tabs.addTab("Thanh toán", createTable(paymentMethodModel));
		content.add(tabs, BorderLayout.CENTER);

		return content;
	}

	private JPanel createStatsPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 5, 10, 0));
		panel.setOpaque(false);
		panel.add(createStat("Tổng số", totalAppointmentsValue));
		panel.add(createStat("Chờ xử lý", pendingValue));
		panel.add(createStat("Đã xác nhận", confirmedValue));
		panel.add(createStat("Hoàn thành", completedValue));
		panel.add(createStat("Đã hủy", cancelledValue));
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
					"Ngày phải nhập theo định dạng yyyy-MM-dd.",
					"Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
			return;
		}

		statusLabel.setText("Đang tải báo cáo...");
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
					statusLabel.setText("Đã cập nhật");
				} catch (Exception e) {
					statusLabel.setText("Không tải được báo cáo");
					JOptionPane.showMessageDialog(ReportPanel.this,
							"Lỗi tải báo cáo: " + e.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
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
