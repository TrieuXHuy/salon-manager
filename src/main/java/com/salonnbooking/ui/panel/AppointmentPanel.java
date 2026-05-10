package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.EmployeeRequests;
import com.salonnbooking.api.dto.PaymentRequests;
import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.dialog.AppointmentDialog;
import com.salonnbooking.ui.dialog.PaymentSimulationDialog;
import com.salonnbooking.ui.theme.Theme;

import net.miginfocom.swing.MigLayout;

public class AppointmentPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
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

	private JButton btnAdd;
	private JButton btnEdit;
	private JButton btnDelete;
	private JButton btnPay;
	private JButton btnRefresh;
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel lblStatus;

	private Integer selectedAppointmentId;
	private List<CustomerRequests.Response> customers;
	private List<EmployeeRequests.Response> employees;
	private List<ServiceRequests.Response> services;
	private List<AppointmentRequests.Response> appointments;

	public AppointmentPanel() {
		setLayout(new MigLayout("insets 24, fill, wrap 1", "[grow]", "[]18[grow]18[]"));
		setBackground(BG_MAIN);

		add(createHeaderPanel(), "growx");
		add(createTablePanel(), "grow");
		add(createToolbarPanel(), "growx");

		loadInitialData();
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel titleLabel = new JLabel("Đặt lịch hẹn");
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(TEXT_MAIN);

		JLabel subtitle = new JLabel("Quản lý lịch hẹn, nhân viên và thanh toán");
		subtitle.setFont(SUBTITLE_FONT);
		subtitle.setForeground(TEXT_MUTED);

		titleBlock.add(titleLabel);
		titleBlock.add(Box.createVerticalStrut(4));
		titleBlock.add(subtitle);
		panel.add(titleBlock, BorderLayout.WEST);
		return panel;
	}

	private JPanel createToolbarPanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(12, 12, 12, 12));

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		btnPanel.setOpaque(false);

		btnAdd = createButton("Thêm", e -> onAddButtonClicked());
		btnEdit = createButton("Sửa", e -> onEditButtonClicked());
		btnDelete = createButton("Xóa", e -> onDeleteButtonClicked());
		btnPay = createButton("Thanh toán", e -> onPayButtonClicked());
		btnRefresh = createButton("Làm mới", e -> loadAppointments());

		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnPay.setEnabled(false);

		btnPanel.add(btnAdd);
		btnPanel.add(btnEdit);
		btnPanel.add(btnDelete);
		btnPanel.add(btnPay);
		btnPanel.add(btnRefresh);

		panel.add(btnPanel, BorderLayout.WEST);

		lblStatus = new JLabel("Sẵn sàng");
		lblStatus.setFont(SUBTITLE_FONT);
		lblStatus.setForeground(TEXT_MUTED);
		panel.add(lblStatus, BorderLayout.EAST);

		return panel;
	}

	private JPanel createTablePanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout(12, 12));
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));

		JLabel title = new JLabel("Danh sách lịch hẹn");
		title.setFont(new Font("Segoe UI", Font.BOLD, 14));
		title.setForeground(TEXT_MAIN);
		panel.add(title, BorderLayout.NORTH);

		String[] columnNames = { "ID", "Khách hàng", "Nhân viên", "Dịch vụ", "Ngày giờ", "Trạng thái", "Tổng tiền" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(32);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoCreateRowSorter(true);
		table.setFont(TABLE_FONT);
		table.setForeground(TEXT_MAIN);
		table.setShowVerticalLines(false);
		table.setGridColor(BORDER);
		table.getTableHeader().setBackground(new Color(245, 243, 255));
		table.getTableHeader().setForeground(TEXT_MUTED);
		table.getTableHeader().setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
		sorter.setComparator(4, Comparator.comparing(value -> LocalDateTime.parse(value.toString(), DATE_FORMATTER)));
		table.setRowSorter(sorter);

		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				selectedAppointmentId = (Integer) tableModel.getValueAt(modelRow, 0);
				btnEdit.setEnabled(true);
				btnDelete.setEnabled(true);
				AppointmentRequests.Response selected = getSelectedAppointment();
				btnPay.setEnabled(selected != null && selected.status() == com.salonnbooking.domain.AppointmentStatus.COMPLETED);
			} else {
				selectedAppointmentId = null;
				btnEdit.setEnabled(false);
				btnDelete.setEnabled(false);
				btnPay.setEnabled(false);
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private void loadInitialData() {
		setStatus("Đang tải dữ liệu...");
		disableButtons();

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				customers = ApiClient.getAllCustomers();
				employees = ApiClient.getActiveEmployees();
				services = ApiClient.getAllServices();
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi tải dữ liệu ban đầu", e);
					setStatus("Lỗi");
					enableButtons();
				}
			}
		};

		worker.execute();
	}

	private void loadAppointments() {
		setStatus("Đang tải lịch hẹn...");

		SwingWorker<List<AppointmentRequests.Response>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<AppointmentRequests.Response> doInBackground() throws Exception {
				return ApiClient.getAllAppointments();
			}

			@Override
			protected void done() {
				try {
					appointments = sortByAppointmentTime(get());
					refreshTable(appointments);
					setStatus("Sẵn sàng - " + appointments.size() + " lịch hẹn");
					enableButtons();
				} catch (Exception e) {
					handleException("Lỗi tải lịch hẹn", e);
					setStatus("Lỗi tải dữ liệu");
				}
			}
		};

		worker.execute();
	}

	private void refreshTable(List<AppointmentRequests.Response> appointments) {
		tableModel.setRowCount(0);

		for (AppointmentRequests.Response apt : appointments) {
			String customerName = customers.stream()
					.filter(c -> c.id().equals(apt.customerId()))
					.map(CustomerRequests.Response::fullName)
					.findFirst()
					.orElse("Không rõ");

			tableModel.addRow(new Object[] {
					apt.id(),
					customerName,
					apt.employeeName(),
					apt.serviceSummary(),
					apt.appointmentTime().format(DATE_FORMATTER),
					apt.status(),
					apt.subtotal()
			});
		}

		selectedAppointmentId = null;
		table.clearSelection();
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnPay.setEnabled(false);
	}

	private List<AppointmentRequests.Response> sortByAppointmentTime(List<AppointmentRequests.Response> source) {
		List<AppointmentRequests.Response> sorted = new ArrayList<>(source);
		sorted.sort(Comparator.comparing(AppointmentRequests.Response::appointmentTime).thenComparing(AppointmentRequests.Response::id));
		return sorted;
	}

	private void onAddButtonClicked() {
		AppointmentDialog dialog = new AppointmentDialog(SwingUtilities.getWindowAncestor(this), customers, employees, services);
		dialog.setVisible(true);
		if (dialog.isApproved()) {
			addAppointment(dialog.getAppointmentCreateRequest());
		}
	}

	private void onEditButtonClicked() {
		AppointmentRequests.Response selectedApt = getSelectedAppointment();
		if (selectedApt == null) {
			showError("Vui lòng chọn một lịch hẹn để sửa");
			return;
		}

		AppointmentDialog dialog = new AppointmentDialog(
				SwingUtilities.getWindowAncestor(this),
				customers,
				employees,
				services,
				selectedApt);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			updateAppointment(selectedApt.id(), dialog.getAppointmentUpdateRequest());
		}
	}

	private void onDeleteButtonClicked() {
		if (selectedAppointmentId == null) {
			showError("Vui lòng chọn một lịch hẹn để xóa");
			return;
		}
		int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa lịch hẹn này?", "Xác nhận xóa",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			deleteAppointment(selectedAppointmentId);
		}
	}

	private void onPayButtonClicked() {
		AppointmentRequests.Response selectedApt = getSelectedAppointment();
		if (selectedApt == null) {
			showError("Vui lòng chọn một lịch hẹn để thanh toán");
			return;
		}
		if (selectedApt.status() != com.salonnbooking.domain.AppointmentStatus.COMPLETED) {
			showError("Chỉ lịch hẹn đã hoàn thành mới được thanh toán");
			return;
		}

		String customerName = findCustomerName(selectedApt.customerId());
		BigDecimal amount = selectedApt.subtotal() != null ? selectedApt.subtotal() : BigDecimal.ZERO;

		PaymentSimulationDialog dialog = new PaymentSimulationDialog(
				SwingUtilities.getWindowAncestor(this),
				selectedApt.id(),
				customerName,
				selectedApt.serviceSummary(),
				amount,
				"QR / chuyển khoản");
		dialog.setVisible(true);

		if (dialog.isPaid()) {
			createSimulatedPayment(selectedApt.id(), amount, PaymentMethod.BANK_TRANSFER);
		}
	}

	private void addAppointment(AppointmentRequests.Create createReq) {
		setStatus("Đang tạo lịch hẹn...");
		disableButtons();
		SwingWorker<AppointmentRequests.Response, Void> worker = new SwingWorker<>() {
			@Override
			protected AppointmentRequests.Response doInBackground() throws Exception {
				return ApiClient.createAppointment(createReq);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(AppointmentPanel.this, "Lịch hẹn được tạo thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi tạo lịch hẹn", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};
		worker.execute();
	}

	private void updateAppointment(Integer appointmentId, AppointmentRequests.Update updateReq) {
		setStatus("Đang cập nhật lịch hẹn...");
		disableButtons();
		SwingWorker<AppointmentRequests.Response, Void> worker = new SwingWorker<>() {
			@Override
			protected AppointmentRequests.Response doInBackground() throws Exception {
				return ApiClient.updateAppointment(appointmentId, updateReq);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(AppointmentPanel.this, "Lịch hẹn được cập nhật thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi cập nhật lịch hẹn", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};
		worker.execute();
	}

	private void deleteAppointment(Integer appointmentId) {
		setStatus("Đang xóa lịch hẹn...");
		disableButtons();
		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				ApiClient.deleteAppointment(appointmentId);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(AppointmentPanel.this, "Lịch hẹn được xóa thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi xóa lịch hẹn", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};
		worker.execute();
	}

	private void createSimulatedPayment(Integer appointmentId, BigDecimal amount, PaymentMethod paymentMethod) {
		setStatus("Đang ghi nhận thanh toán...");
		disableButtons();

		PaymentRequests.Create request = new PaymentRequests.Create(
				appointmentId,
				amount,
				BigDecimal.ZERO,
				amount,
				paymentMethod,
				PaymentStatus.PAID,
				LocalDateTime.now());

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				ApiClient.createPayment(request);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(AppointmentPanel.this, "Thanh toán mô phỏng thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi ghi nhận thanh toán", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};
		worker.execute();
	}

	private AppointmentRequests.Response getSelectedAppointment() {
		if (selectedAppointmentId == null || appointments == null) {
			return null;
		}
		return appointments.stream().filter(a -> a.id().equals(selectedAppointmentId)).findFirst().orElse(null);
	}

	private String findCustomerName(Integer customerId) {
		if (customers == null) {
			return "Không rõ";
		}
		return customers.stream()
				.filter(c -> c.id().equals(customerId))
				.map(CustomerRequests.Response::fullName)
				.findFirst()
				.orElse("Không rõ");
	}

	private void handleException(String title, Exception e) {
		String message = e.getMessage();
		if (message == null || message.isEmpty()) {
			message = e.getClass().getSimpleName();
		}
		JOptionPane.showMessageDialog(this, title + ": " + message, "Lỗi", JOptionPane.ERROR_MESSAGE);
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
	}

	private void setStatus(String status) {
		lblStatus.setText(status);
	}

	private void disableButtons() {
		btnAdd.setEnabled(false);
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnPay.setEnabled(false);
		btnRefresh.setEnabled(false);
	}

	private void enableButtons() {
		btnAdd.setEnabled(true);
		btnRefresh.setEnabled(true);
		if (selectedAppointmentId != null) {
			btnEdit.setEnabled(true);
			btnDelete.setEnabled(true);
			AppointmentRequests.Response selected = getSelectedAppointment();
			btnPay.setEnabled(selected != null && selected.status() == com.salonnbooking.domain.AppointmentStatus.COMPLETED);
		}
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
}
