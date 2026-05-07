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
import com.salonnbooking.domain.AppointmentStatus;

/**
 * AppointmentPanel - Quản lý lịch hẹn
 * 
 * Tích hợp SwingWorker để gọi API không làm block EDT
 * Xử lý LocalDateTime cho appointment time
 */
public class AppointmentPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	// UI Components
	private JComboBox<ComboBoxCustomer> cbCustomer;
	private JComboBox<ComboBoxService> cbService;
	private JTextField tfAppointmentDateTime;
	private JComboBox<AppointmentStatus> cbStatus;
	private JTextArea taNote;
	private JTable table;
	private DefaultTableModel tableModel;

	private Integer selectedAppointmentId = null;
	private List<CustomerRequests.Response> customers;
	private List<ServiceRequests.Response> services;

	public AppointmentPanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(UIManager.getColor("Panel.background"));

		// Header
		add(createHeaderPanel(), BorderLayout.NORTH);

		// Main Content
		add(createMainPanel(), BorderLayout.CENTER);

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
	 * Tạo panel chính
	 */
	private JPanel createMainPanel() {
		JPanel main = new JPanel(new BorderLayout(10, 10));
		main.setOpaque(false);

		// Form Panel
		main.add(createFormPanel(), BorderLayout.NORTH);

		// Table Panel
		main.add(createTablePanel(), BorderLayout.CENTER);

		return main;
	}

	/**
	 * Tạo form input
	 */
	private JPanel createFormPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createTitledBorder("Appointment Information"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Row 1: Customer & Service
		addLabel(panel, "Customer:", 0, 0, gbc);
		cbCustomer = new JComboBox<>();
		cbCustomer.setPreferredSize(new Dimension(200, 30));
		panel.add(cbCustomer, setPosition(gbc, 1, 0));

		addLabel(panel, "Service:", 2, 0, gbc);
		cbService = new JComboBox<>();
		cbService.setPreferredSize(new Dimension(200, 30));
		panel.add(cbService, setPosition(gbc, 3, 0));

		// Row 2: Date/Time & Status
		addLabel(panel, "Appointment Date/Time:", 0, 1, gbc);
		tfAppointmentDateTime = new JTextField(20);
		tfAppointmentDateTime.setToolTipText("Format: yyyy-MM-dd HH:mm");
		panel.add(tfAppointmentDateTime, setPosition(gbc, 1, 1));

		addLabel(panel, "Status:", 2, 1, gbc);
		cbStatus = new JComboBox<>(AppointmentStatus.values());
		cbStatus.setPreferredSize(new Dimension(100, 30));
		panel.add(cbStatus, setPosition(gbc, 3, 1));

		// Row 3: Notes
		addLabel(panel, "Notes:", 0, 2, gbc);
		taNote = new JTextArea(3, 40);
		taNote.setLineWrap(true);
		taNote.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(taNote);
		panel.add(scrollPane, setPosition(gbc, 1, 2));

		// Buttons
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		btnPanel.setOpaque(false);

		JButton btnAdd = createButton("Add", e -> addAppointment());
		JButton btnUpdate = createButton("Update", e -> updateAppointment());
		JButton btnDelete = createButton("Delete", e -> deleteAppointment());
		JButton btnClear = createButton("Clear", e -> clearForm());
		JButton btnRefresh = createButton("Refresh", e -> loadAppointments());

		btnPanel.add(btnAdd);
		btnPanel.add(btnUpdate);
		btnPanel.add(btnDelete);
		btnPanel.add(btnClear);
		btnPanel.add(btnRefresh);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		panel.add(btnPanel, gbc);

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
				return false;
			}
		};

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(25);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
				loadFormFromTable();
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Load dữ liệu ban đầu (Customers, Services)
	 */
	private void loadInitialData() {
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
					// Populate ComboBoxes
					for (CustomerRequests.Response c : customers) {
						cbCustomer.addItem(new ComboBoxCustomer(c.id(), c.fullName()));
					}
					for (ServiceRequests.Response s : services) {
						cbService.addItem(new ComboBoxService(s.id(), s.name()));
					}
					// Load appointments
					loadAppointments();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Error loading initial data: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Load danh sách lịch hẹn từ API
	 */
	private void loadAppointments() {
		SwingWorker<List<AppointmentRequests.Response>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<AppointmentRequests.Response> doInBackground() throws Exception {
				return ApiClient.getAllAppointments();
			}

			@Override
			protected void done() {
				try {
					List<AppointmentRequests.Response> appointments = get();
					refreshTable(appointments);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Error loading appointments: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Làm mới bảng
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
					apt.note()
			});
		}
	}

	/**
	 * Load form từ hàng được chọn
	 */
	private void loadFormFromTable() {
		int row = table.getSelectedRow();
		if (row >= 0) {
			selectedAppointmentId = (Integer) tableModel.getValueAt(row, 0);
			// Load appointment detail
			loadAppointmentDetail(selectedAppointmentId);
		}
	}

	/**
	 * Load chi tiết appointment từ API
	 */
	private void loadAppointmentDetail(Integer appointmentId) {
		SwingWorker<AppointmentRequests.Response, Void> worker = new SwingWorker<>() {
			@Override
			protected AppointmentRequests.Response doInBackground() throws Exception {
				return ApiClient.getAppointment(appointmentId);
			}

			@Override
			protected void done() {
				try {
					AppointmentRequests.Response apt = get();
					// Populate form fields
					cbCustomer.setSelectedItem(new ComboBoxCustomer(apt.customerId(), ""));
					cbService.setSelectedItem(new ComboBoxService(apt.serviceId(), ""));
					tfAppointmentDateTime.setText(apt.appointmentTime().format(DATE_FORMATTER));
					cbStatus.setSelectedItem(apt.status());
					taNote.setText(apt.note());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Error loading appointment detail: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Thêm lịch hẹn mới
	 */
	private void addAppointment() {
		if (!validateForm()) {
			return;
		}

		try {
			ComboBoxCustomer selectedCustomer = (ComboBoxCustomer) cbCustomer.getSelectedItem();
			ComboBoxService selectedService = (ComboBoxService) cbService.getSelectedItem();
			LocalDateTime appointmentTime = LocalDateTime.parse(tfAppointmentDateTime.getText(),
					DATE_FORMATTER);

			var createReq = new AppointmentRequests.Create(
					selectedCustomer.getId(),
					selectedService.getId(),
					appointmentTime,
					(AppointmentStatus) cbStatus.getSelectedItem(),
					taNote.getText());

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
								"Appointment created successfully!", "Success",
								JOptionPane.INFORMATION_MESSAGE);
						clearForm();
						loadAppointments();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(AppointmentPanel.this,
								"Error creating appointment: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			};

			worker.execute();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Invalid date format. Use: yyyy-MM-dd HH:mm",
					"Validation Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Cập nhật lịch hẹn
	 */
	private void updateAppointment() {
		if (selectedAppointmentId == null) {
			JOptionPane.showMessageDialog(this, "Please select an appointment to update",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!validateForm()) {
			return;
		}

		try {
			ComboBoxCustomer selectedCustomer = (ComboBoxCustomer) cbCustomer.getSelectedItem();
			ComboBoxService selectedService = (ComboBoxService) cbService.getSelectedItem();
			LocalDateTime appointmentTime = LocalDateTime.parse(tfAppointmentDateTime.getText(),
					DATE_FORMATTER);

			var updateReq = new AppointmentRequests.Update(
					selectedCustomer.getId(),
					selectedService.getId(),
					appointmentTime,
					(AppointmentStatus) cbStatus.getSelectedItem(),
					taNote.getText());

			int appointmentId = selectedAppointmentId;

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
								"Appointment updated successfully!", "Success",
								JOptionPane.INFORMATION_MESSAGE);
						clearForm();
						loadAppointments();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(AppointmentPanel.this,
								"Error updating appointment: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			};

			worker.execute();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Invalid date format. Use: yyyy-MM-dd HH:mm",
					"Validation Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Xóa lịch hẹn
	 */
	private void deleteAppointment() {
		if (selectedAppointmentId == null) {
			JOptionPane.showMessageDialog(this, "Please select an appointment to delete",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to delete this appointment?",
				"Confirm Delete", JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		int appointmentId = selectedAppointmentId;

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
							"Appointment deleted successfully!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadAppointments();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Error deleting appointment: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Xóa form
	 */
	private void clearForm() {
		cbCustomer.setSelectedIndex(0);
		cbService.setSelectedIndex(0);
		tfAppointmentDateTime.setText("");
		cbStatus.setSelectedIndex(0);
		taNote.setText("");
		selectedAppointmentId = null;
		table.clearSelection();
	}

	/**
	 * Kiểm tra tính hợp lệ form
	 */
	private boolean validateForm() {
		if (cbCustomer.getSelectedIndex() < 0) {
			JOptionPane.showMessageDialog(this, "Please select a customer", "Validation Error",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (cbService.getSelectedIndex() < 0) {
			JOptionPane.showMessageDialog(this, "Please select a service", "Validation Error",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (tfAppointmentDateTime.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter appointment date/time",
					"Validation Error", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	// ========== HELPER CLASSES & METHODS ==========

	/**
	 * Wrapper class cho ComboBox Customer
	 */
	private static class ComboBoxCustomer {
		private final Integer id;
		private final String name;

		public ComboBoxCustomer(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ComboBoxCustomer)) return false;
			ComboBoxCustomer that = (ComboBoxCustomer) o;
			return id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	/**
	 * Wrapper class cho ComboBox Service
	 */
	private static class ComboBoxService {
		private final Integer id;
		private final String name;

		public ComboBoxService(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ComboBoxService)) return false;
			ComboBoxService that = (ComboBoxService) o;
			return id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	private void addLabel(JPanel panel, String text, int x, int y, GridBagConstraints gbc) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		panel.add(new JLabel(text), gbc);
	}

	private GridBagConstraints setPosition(GridBagConstraints gbc, int x, int y) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		return gbc;
	}

	private JButton createButton(String label, java.awt.event.ActionListener listener) {
		JButton btn = new JButton(label);
		btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		btn.setFocusPainted(false);
		btn.addActionListener(listener);
		return btn;
	}
}
