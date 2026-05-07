package com.salonnbooking.ui.dialog;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.domain.AppointmentStatus;

/**
 * AppointmentDialog - Form dialog để thêm/sửa lịch hẹn
 * Modal dialog với validation & error handling
 */
public class AppointmentDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private JComboBox<ComboBoxItem> cbCustomer;
	private JComboBox<ComboBoxItem> cbService;
	private JTextField tfDateTime;
	private JComboBox<AppointmentStatus> cbStatus;
	private JTextArea taNote;
	private JButton btnSave;
	private JButton btnCancel;

	private boolean approved = false;
	private AppointmentRequests.Response editingAppointment = null;

	/**
	 * Constructor - Tạo dialog (Add mode)
	 */
	public AppointmentDialog(Window owner, List<CustomerRequests.Response> customers,
			List<ServiceRequests.Response> services) {
		this(owner, customers, services, null);
	}

	/**
	 * Constructor - Tạo dialog (Edit mode)
	 */
	public AppointmentDialog(Window owner, List<CustomerRequests.Response> customers,
			List<ServiceRequests.Response> services, AppointmentRequests.Response editingAppointment) {
		super(owner, "Appointment", ModalityType.APPLICATION_MODAL);
		this.editingAppointment = editingAppointment;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(500, 450);
		setLocationRelativeTo(owner);
		setResizable(false);

		JPanel contentPane = createContentPane(customers, services);
		setContentPane(contentPane);

		// Populate data if editing
		if (editingAppointment != null) {
			populateFormData(customers, services, editingAppointment);
		}
	}

	/**
	 * Tạo content panel
	 */
	private JPanel createContentPane(List<CustomerRequests.Response> customers,
			List<ServiceRequests.Response> services) {
		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setBackground(UIManager.getColor("Panel.background"));

		// Form Panel
		contentPane.add(createFormPanel(customers, services), BorderLayout.CENTER);

		// Button Panel
		contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

		return contentPane;
	}

	/**
	 * Tạo form input panel
	 */
	private JPanel createFormPanel(List<CustomerRequests.Response> customers,
			List<ServiceRequests.Response> services) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Customer ComboBox
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		panel.add(new JLabel("Customer:"), gbc);

		cbCustomer = new JComboBox<>();
		for (CustomerRequests.Response c : customers) {
			cbCustomer.addItem(new ComboBoxItem(c.id(), c.fullName()));
		}
		cbCustomer.setRenderer(new ComboBoxRenderer());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbCustomer, gbc);

		// Service ComboBox
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		panel.add(new JLabel("Service:"), gbc);

		cbService = new JComboBox<>();
		for (ServiceRequests.Response s : services) {
			cbService.addItem(new ComboBoxItem(s.id(), s.name()));
		}
		cbService.setRenderer(new ComboBoxRenderer());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbService, gbc);

		// DateTime TextField
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		panel.add(new JLabel("Date/Time:"), gbc);

		tfDateTime = new JTextField();
		tfDateTime.setToolTipText("Format: yyyy-MM-dd HH:mm (e.g., 2026-12-31 14:30)");
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(tfDateTime, gbc);

		// Status ComboBox
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		panel.add(new JLabel("Status:"), gbc);

		cbStatus = new JComboBox<>(AppointmentStatus.values());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbStatus, gbc);

		// Note TextArea
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		panel.add(new JLabel("Note:"), gbc);

		taNote = new JTextArea(5, 30);
		taNote.setLineWrap(true);
		taNote.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(taNote);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		panel.add(scrollPane, gbc);

		return panel;
	}

	/**
	 * Tạo button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setOpaque(false);

		btnSave = new JButton(editingAppointment != null ? "Update" : "Create");
		btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		btnSave.addActionListener(e -> onSave());

		btnCancel = new JButton("Cancel");
		btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		btnCancel.addActionListener(e -> dispose());

		panel.add(btnSave);
		panel.add(btnCancel);

		return panel;
	}

	/**
	 * Populate form khi sửa
	 */
	private void populateFormData(List<CustomerRequests.Response> customers,
			List<ServiceRequests.Response> services, AppointmentRequests.Response apt) {
		// Set customer
		for (int i = 0; i < cbCustomer.getItemCount(); i++) {
			ComboBoxItem item = cbCustomer.getItemAt(i);
			if (item.getId().equals(apt.customerId())) {
				cbCustomer.setSelectedIndex(i);
				break;
			}
		}

		// Set service
		for (int i = 0; i < cbService.getItemCount(); i++) {
			ComboBoxItem item = cbService.getItemAt(i);
			if (item.getId().equals(apt.serviceId())) {
				cbService.setSelectedIndex(i);
				break;
			}
		}

		// Set datetime
		tfDateTime.setText(apt.appointmentTime().format(DATE_FORMATTER));

		// Set status
		cbStatus.setSelectedItem(apt.status());

		// Set note
		if (apt.note() != null) {
			taNote.setText(apt.note());
		}
	}

	/**
	 * Handle Save button
	 */
	private void onSave() {
		if (!validateForm()) {
			return;
		}

		approved = true;
		dispose();
	}

	/**
	 * Validate form data
	 */
	private boolean validateForm() {
		if (cbCustomer.getSelectedIndex() < 0) {
			showError("Please select a customer");
			return false;
		}

		if (cbService.getSelectedIndex() < 0) {
			showError("Please select a service");
			return false;
		}

		String dateTimeStr = tfDateTime.getText().trim();
		if (dateTimeStr.isEmpty()) {
			showError("Please enter appointment date/time");
			return false;
		}

		try {
			LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
		} catch (DateTimeParseException e) {
			showError("Invalid date/time format.\nExpected: yyyy-MM-dd HH:mm\nExample: 2026-12-31 14:30");
			return false;
		}

		return true;
	}

	/**
	 * Show error message
	 */
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Validation Error",
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Get appointment data từ form
	 */
	public AppointmentRequests.Create getAppointmentCreateRequest() {
		ComboBoxItem customer = (ComboBoxItem) cbCustomer.getSelectedItem();
		ComboBoxItem service = (ComboBoxItem) cbService.getSelectedItem();
		LocalDateTime dateTime = LocalDateTime.parse(tfDateTime.getText(), DATE_FORMATTER);

		return new AppointmentRequests.Create(
				customer.getId(),
				service.getId(),
				dateTime,
				(AppointmentStatus) cbStatus.getSelectedItem(),
				taNote.getText().trim());
	}

	/**
	 * Get appointment update request
	 */
	public AppointmentRequests.Update getAppointmentUpdateRequest() {
		ComboBoxItem customer = (ComboBoxItem) cbCustomer.getSelectedItem();
		ComboBoxItem service = (ComboBoxItem) cbService.getSelectedItem();
		LocalDateTime dateTime = LocalDateTime.parse(tfDateTime.getText(), DATE_FORMATTER);

		return new AppointmentRequests.Update(
				customer.getId(),
				service.getId(),
				dateTime,
				(AppointmentStatus) cbStatus.getSelectedItem(),
				taNote.getText().trim());
	}

	/**
	 * Check nếu user approved (clicked Save)
	 */
	public boolean isApproved() {
		return approved;
	}

	/**
	 * Helper class cho ComboBox item
	 */
	public static class ComboBoxItem {
		private final Integer id;
		private final String text;

		public ComboBoxItem(Integer id, String text) {
			this.id = id;
			this.text = text;
		}

		public Integer getId() {
			return id;
		}

		@Override
		public String toString() {
			return text;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ComboBoxItem)) return false;
			ComboBoxItem that = (ComboBoxItem) o;
			return id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	/**
	 * Custom ListCellRenderer cho ComboBox
	 */
	private static class ComboBoxRenderer extends JLabel implements javax.swing.ListCellRenderer<Object> {
		private static final long serialVersionUID = 1L;

		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			if (value != null) {
				setText(value.toString());
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			return this;
		}
	}
}
