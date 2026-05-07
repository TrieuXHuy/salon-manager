package com.salonnbooking.ui.dialog;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
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

	private JComboBox<ComboBoxItem> cbCustomer;
	private JComboBox<ComboBoxItem> cbService;
	private JSpinner spDateTime;
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
		super(owner, "Lịch hẹn", ModalityType.APPLICATION_MODAL);
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
		panel.add(new JLabel("Khách hàng:"), gbc);

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
		panel.add(new JLabel("Dịch vụ:"), gbc);

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
		panel.add(new JLabel("Ngày/Giờ:"), gbc);

		// Chọn ngày giờ bằng spinner thay vì bắt người dùng gõ đúng format.
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(createDateTimePicker(), gbc);

		// Status ComboBox
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		panel.add(new JLabel("Trạng thái:"), gbc);

		cbStatus = new JComboBox<>(AppointmentStatus.values());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbStatus, gbc);

		// Note TextArea
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		panel.add(new JLabel("Ghi chú:"), gbc);

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

	private JPanel createDateTimePicker() {
		JPanel panel = new JPanel(new BorderLayout(6, 0));
		panel.setOpaque(false);

		spDateTime = new JSpinner(new SpinnerDateModel(
				Date.from(defaultAppointmentTime().atZone(ZoneId.systemDefault()).toInstant()),
				null,
				null,
				Calendar.MINUTE));
		JSpinner.DateEditor editor = new JSpinner.DateEditor(spDateTime, "dd/MM/yyyy HH:mm");
		spDateTime.setEditor(editor);
		spDateTime.setToolTipText("Chọn ngày giờ, ví dụ 10/05/2026 09:00");
		panel.add(spDateTime, BorderLayout.CENTER);

		JPanel quickButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		quickButtons.setOpaque(false);

		JButton nowButton = new JButton("Bây giờ");
		nowButton.addActionListener(e -> spDateTime.setValue(new Date()));
		quickButtons.add(nowButton);

		JButton plus30Button = new JButton("+30 phút");
		plus30Button.addActionListener(e -> spDateTime.setValue(Date.from(
				getSelectedAppointmentTime().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant())));
		quickButtons.add(plus30Button);

		panel.add(quickButtons, BorderLayout.EAST);
		return panel;
	}

	/**
	 * Tạo button panel
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setOpaque(false);

		btnSave = new JButton(editingAppointment != null ? "Cập nhật" : "Tạo mới");
		btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		btnSave.addActionListener(e -> onSave());

		btnCancel = new JButton("Hủy");
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
		spDateTime.setValue(Date.from(apt.appointmentTime().atZone(ZoneId.systemDefault()).toInstant()));

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
			showError("Vui lòng chọn khách hàng");
			return false;
		}

		if (cbService.getSelectedIndex() < 0) {
			showError("Vui lòng chọn dịch vụ");
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
		LocalDateTime dateTime = getSelectedAppointmentTime();

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
		LocalDateTime dateTime = getSelectedAppointmentTime();

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

	private LocalDateTime getSelectedAppointmentTime() {
		Date selectedDate = (Date) spDateTime.getValue();
		return LocalDateTime.ofInstant(selectedDate.toInstant(), ZoneId.systemDefault())
				.withSecond(0)
				.withNano(0);
	}

	private LocalDateTime defaultAppointmentTime() {
		LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
		int minutesToAdd = 30 - (now.getMinute() % 30);
		if (minutesToAdd == 30) {
			minutesToAdd = 0;
		}
		return now.plusMinutes(minutesToAdd);
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
