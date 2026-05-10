package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.salonnbooking.api.dto.ReportRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class ReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
	private static final Color BG_MAIN = new Color(248, 250, 252);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);
	private static final Color BORDER = new Color(226, 232, 240);
	private static final Color PRIMARY = new Color(109, 73, 224);
	private static final Color PRIMARY_SOFT = new Color(237, 233, 255);
	private static final Font TITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 24));
	private static final Font SUBTITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12));
	private static final Font TABLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13));

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
		setLayout(new MigLayout("insets 24, fill, wrap 1", "[grow]", "[]18[grow]18[]"));
		setBackground(BG_MAIN);

		add(createHeader(), "growx");
		add(createContent(), "grow");
		add(createFooter(), "growx");

		loadReports();
	}

	private JPanel createHeader() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel title = new JLabel("Báo cáo");
		title.setFont(TITLE_FONT);
		title.setForeground(TEXT_MAIN);

		JLabel subtitle = new JLabel("Tổng hợp số liệu theo khoảng thời gian");
		subtitle.setFont(SUBTITLE_FONT);
		subtitle.setForeground(TEXT_MUTED);

		titleBlock.add(title);
		titleBlock.add(Box.createVerticalStrut(4));
		titleBlock.add(subtitle);
		panel.add(titleBlock, BorderLayout.WEST);

		RoundedPanel controls = new RoundedPanel(14, BG_CARD, true);
		controls.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 6));
		controls.setBorder(new EmptyBorder(6, 8, 6, 8));

		JLabel fromLabel = new JLabel("Từ ngày");
		fromLabel.setFont(SUBTITLE_FONT);
		fromLabel.setForeground(TEXT_MUTED);
		controls.add(fromLabel);
		styleField(startDateField);
		controls.add(startDateField);
		JLabel toLabel = new JLabel("Đến ngày");
		toLabel.setFont(SUBTITLE_FONT);
		toLabel.setForeground(TEXT_MUTED);
		controls.add(toLabel);
		styleField(endDateField);
		controls.add(endDateField);

		JButton refreshButton = createButton("Làm mới", e -> loadReports());
		controls.add(refreshButton);
		panel.add(controls, BorderLayout.EAST);

		return panel;
	}

	private JPanel createContent() {
		JPanel content = new JPanel(new BorderLayout(12, 12));
		content.setOpaque(false);
		content.add(createStatsPanel(), BorderLayout.NORTH);

		RoundedPanel tabsCard = new RoundedPanel(16, BG_CARD, true);
		tabsCard.setLayout(new BorderLayout());
		tabsCard.setBorder(new EmptyBorder(12, 12, 12, 12));

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Doanh thu ngày", createTable(dailyRevenueModel));
		tabs.addTab("Doanh thu dịch vụ", createTable(serviceRevenueModel));
		tabs.addTab("Thanh toán", createTable(paymentMethodModel));
		tabsCard.add(tabs, BorderLayout.CENTER);
		content.add(tabsCard, BorderLayout.CENTER);

		return content;
	}

	private JPanel createStatsPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 5, 12, 0));
		panel.setOpaque(false);
		panel.add(createStat("Tổng số", totalAppointmentsValue));
		panel.add(createStat("Chờ xử lý", pendingValue));
		panel.add(createStat("Đã xác nhận", confirmedValue));
		panel.add(createStat("Hoàn thành", completedValue));
		panel.add(createStat("Đã hủy", cancelledValue));
		return panel;
	}

	private JPanel createStat(String label, JLabel value) {
		RoundedPanel panel = new RoundedPanel(14, BG_CARD, true);
		panel.setLayout(new BorderLayout(4, 4));
		panel.setBorder(new EmptyBorder(10, 12, 10, 12));

		JLabel labelView = new JLabel(label);
		labelView.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		labelView.setForeground(TEXT_MUTED);
		value.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 20)));
		value.setForeground(TEXT_MAIN);

		panel.add(labelView, BorderLayout.NORTH);
		panel.add(value, BorderLayout.CENTER);
		return panel;
	}

	private JScrollPane createTable(DefaultTableModel model) {
		JTable table = new JTable(model);
		table.setRowHeight(32);
		table.setFont(TABLE_FONT);
		table.setForeground(TEXT_MAIN);
		table.setShowVerticalLines(false);
		table.setGridColor(BORDER);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setBackground(new Color(245, 243, 255));
		table.getTableHeader().setForeground(TEXT_MUTED);
		table.getTableHeader().setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
		return scrollPane;
	}

	private JPanel createFooter() {
		JPanel footer = new JPanel(new BorderLayout());
		footer.setOpaque(false);
		statusLabel.setFont(SUBTITLE_FONT);
		statusLabel.setForeground(TEXT_MUTED);
		footer.add(statusLabel, BorderLayout.WEST);
		return footer;
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

	private JButton createButton(String label, java.awt.event.ActionListener listener) {
		JButton btn = new JButton(label);
		btn.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 11)));
		btn.setFocusPainted(false);
		btn.setBackground(PRIMARY_SOFT);
		btn.setForeground(PRIMARY);
		btn.setBorder(new EmptyBorder(6, 12, 6, 12));
		btn.addActionListener(listener);
		return btn;
	}

	private void styleField(JTextField field) {
		field.setBorder(BorderFactory.createLineBorder(BORDER));
		field.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		field.setBackground(Color.WHITE);
		field.setForeground(TEXT_MAIN);
	}

	private record ReportData(
			ReportRequests.AppointmentStatsResponse stats,
			List<ReportRequests.DailyRevenueResponse> dailyRevenue,
			List<ReportRequests.ServiceRevenueResponse> serviceRevenue,
			List<ReportRequests.PaymentMethodResponse> paymentMethods) {
	}
}
