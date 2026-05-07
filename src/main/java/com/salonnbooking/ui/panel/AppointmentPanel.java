package com.salonnbooking.ui.panel;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.ui.dialog.AppointmentDialog;

/**
 * AppointmentPanel - Quản lý lịch hẹn
 * 
 * Tích hợp SwingWorker để gọi API không làm block EDT (Non-blocking UI)
 * Sử dụng AppointmentDialog cho form nhập liệu
 * Xử lý Exception & User Feedback toàn diện
 */
public class AppointmentPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	// UI Components
	private JButton btnAdd;
	private JButton btnEdit;
	private JButton btnDelete;
	private JButton btnRefresh;
	private JTable table;
	private DefaultTableModel tableModel;
	private JLabel lblStatus;

	// Data
	private Integer selectedAppointmentId = null;
	private List<CustomerRequests.Response> customers;
	private List<ServiceRequests.Response> services;
	private List<AppointmentRequests.Response> appointments;

	public AppointmentPanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(UIManager.getColor("Panel.background"));

		// Header
		add(createHeaderPanel(), BorderLayout.NORTH);

		// Main Content (Table)
		add(createTablePanel(), BorderLayout.CENTER);

		// Toolbar
		add(createToolbarPanel(), BorderLayout.SOUTH);

		// Load dữ liệu ban đầu
		loadInitialData();
	}

	/**
	 * Tạo panel header
	 */
	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JLabel titleLabel = new JLabel("Appointment Booking");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		panel.add(titleLabel, BorderLayout.WEST);

		return panel;
	}

	/**
	 * Tạo toolbar với các button
	 */
	private JPanel createToolbarPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		btnPanel.setOpaque(false);

		btnAdd = createButton("Add", e -> onAddButtonClicked());
		btnEdit = createButton("Edit", e -> onEditButtonClicked());
		btnDelete = createButton("Delete", e -> onDeleteButtonClicked());
		btnRefresh = createButton("Refresh", e -> loadAppointments());

		btnAdd.setEnabled(false);
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);

		btnPanel.add(btnAdd);
		btnPanel.add(btnEdit);
		btnPanel.add(btnDelete);
		btnPanel.add(btnRefresh);

		panel.add(btnPanel, BorderLayout.WEST);

		// Status label
		lblStatus = new JLabel("Ready");
		lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 10));
		panel.add(lblStatus, BorderLayout.EAST);

		return panel;
	}

	/**
	 * Tạo panel bảng danh sách
	 */
	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createTitledBorder("Appointment List"));

		String[] columnNames = { "ID", "Customer", "Service", "Date/Time", "Status", "Note" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Read-only table
			}
		};

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(25);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Row selection listener
		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
				selectedAppointmentId = (Integer) tableModel.getValueAt(table.getSelectedRow(), 0);
				btnEdit.setEnabled(true);
				btnDelete.setEnabled(true);
			} else {
				selectedAppointmentId = null;
				btnEdit.setEnabled(false);
				btnDelete.setEnabled(false);
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	// ========== DATA LOADING METHODS ==========

	/**
	 * Load dữ liệu ban đầu (Customers, Services, Appointments)
	 */
	private void loadInitialData() {
		setStatus("Loading initial data...");
		disableButtons();

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				customers = ApiClient.getAllCustomers();
				services = ApiClient.getAllServices();
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					loadAppointments();
				} catch (Exception e) {
					handleException("Error loading initial data", e);
					setStatus("Error");
					enableButtons();
				}
			}
		};

		worker.execute();
	}

	/**
	 * Load danh sách lịch hẹn từ API (Non-blocking)
	 */
	private void loadAppointments() {
		setStatus("Loading appointments...");

		SwingWorker<List<AppointmentRequests.Response>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<AppointmentRequests.Response> doInBackground() throws Exception {
				return ApiClient.getAllAppointments();
			}

			@Override
			protected void done() {
				try {
					appointments = get();
					refreshTable(appointments);
					setStatus("Ready - " + appointments.size() + " appointments");
					enableButtons();
				} catch (Exception e) {
					handleException("Error loading appointments", e);
					setStatus("Error loading data");
				}
			}
		};

		worker.execute();
	}

	/**
	 * Làm mới bảng từ danh sách appointments
	 */
	private void refreshTable(List<AppointmentRequests.Response> appointments) {
		tableModel.setRowCount(0);

		for (AppointmentRequests.Response apt : appointments) {
			String customerName = customers.stream()
					.filter(c -> c.id().equals(apt.customerId()))
					.map(CustomerRequests.Response::fullName)
					.findFirst()
					.orElse("Unknown");

			String serviceName = services.stream()
					.filter(s -> s.id().equals(apt.serviceId()))
					.map(ServiceRequests.Response::name)
					.findFirst()
					.orElse("Unknown");

			tableModel.addRow(new Object[] {
					apt.id(),
					customerName,
					serviceName,
					apt.appointmentTime().format(DATE_FORMATTER),
					apt.status(),
					apt.note() != null ? apt.note() : ""
			});
		}

		// Clear selection
		selectedAppointmentId = null;
		table.clearSelection();
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
	}

	// ========== BUTTON CLICK HANDLERS ==========

	/**
	 * Xử lý button Add
	 */
	private void onAddButtonClicked() {
		AppointmentDialog dialog = new AppointmentDialog(
				SwingUtilities.getWindowAncestor(this),
				customers, services);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			addAppointment(dialog.getAppointmentCreateRequest());
		}
	}

	/**
	 * Xử lý button Edit
	 */
	private void onEditButtonClicked() {
		if (selectedAppointmentId == null) {
			JOptionPane.showMessageDialog(this,
					"Please select an appointment to edit",
					"No Selection", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Find the appointment
		AppointmentRequests.Response selectedApt = appointments.stream()
				.filter(a -> a.id().equals(selectedAppointmentId))
				.findFirst()
				.orElse(null);

		if (selectedApt == null) {
			showError("Appointment not found");
			return;
		}

		AppointmentDialog dialog = new AppointmentDialog(
				SwingUtilities.getWindowAncestor(this),
				customers, services, selectedApt);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			updateAppointment(selectedAppointmentId, dialog.getAppointmentUpdateRequest());
		}
	}

	/**
	 * Xử lý button Delete
	 */
	private void onDeleteButtonClicked() {
		if (selectedAppointmentId == null) {
			JOptionPane.showMessageDialog(this,
					"Please select an appointment to delete",
					"No Selection", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to delete this appointment?",
				"Confirm Delete", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			deleteAppointment(selectedAppointmentId);
		}
	}

	// ========== CRUD OPERATIONS (Non-blocking with SwingWorker) ==========

	/**
	 * Thêm lịch hẹn mới (Non-blocking)
	 */
	private void addAppointment(AppointmentRequests.Create createReq) {
		setStatus("Creating appointment...");
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
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Appointment created successfully!",
							"Success", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Error creating appointment", e);
					enableButtons();
					setStatus("Error");
				}
			}
		};

		worker.execute();
	}

	/**
	 * Cập nhật lịch hẹn (Non-blocking)
	 */
	private void updateAppointment(Integer appointmentId, AppointmentRequests.Update updateReq) {
		setStatus("Updating appointment...");
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
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Appointment updated successfully!",
							"Success", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Error updating appointment", e);
					enableButtons();
					setStatus("Error");
				}
			}
		};

		worker.execute();
	}

	/**
	 * Xóa lịch hẹn (Non-blocking)
	 */
	private void deleteAppointment(Integer appointmentId) {
		setStatus("Deleting appointment...");
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
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Appointment deleted successfully!",
							"Success", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Error deleting appointment", e);
					enableButtons();
					setStatus("Error");
				}
			}
		};

		worker.execute();
	}

	// ========== HELPER METHODS ==========

	/**
	 * Xử lý exception và hiển thị thông báo lỗi
	 */
	private void handleException(String title, Exception e) {
		String message = e.getMessage();
		if (message == null || message.isEmpty()) {
			message = e.getClass().getSimpleName();
		}

		JOptionPane.showMessageDialog(this,
				title + ": " + message,
				"Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Hiển thị thông báo lỗi
	 */
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Cập nhật status label
	 */
	private void setStatus(String status) {
		lblStatus.setText(status);
	}

	/**
	 * Disable tất cả buttons
	 */
	private void disableButtons() {
		btnAdd.setEnabled(false);
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnRefresh.setEnabled(false);
	}

	/**
	 * Enable tất cả buttons
	 */
	private void enableButtons() {
		btnAdd.setEnabled(true);
		btnRefresh.setEnabled(true);
		// Edit và Delete chỉ enable khi có selection
		if (selectedAppointmentId != null) {
			btnEdit.setEnabled(true);
			btnDelete.setEnabled(true);
		}
	}

	/**
	 * Tạo button với listener
	 */
	private JButton createButton(String label, java.awt.event.ActionListener listener) {
		JButton btn = new JButton(label);
		btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		btn.setFocusPainted(false);
		btn.addActionListener(listener);
		return btn;
	}
}
