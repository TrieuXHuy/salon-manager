package com.salonnbooking.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.EmployeeRequests;
import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.domain.AppointmentStatus;

public class AppointmentDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JComboBox<ComboBoxItem> cbCustomer;
	private JComboBox<ComboBoxItem> cbEmployee;
	private JList<ComboBoxItem> serviceList;
	private JSpinner spDateTime;
	private JComboBox<AppointmentStatus> cbStatus;
	private JTextArea taNote;
	private boolean approved;
	private final AppointmentRequests.Response editingAppointment;

	public AppointmentDialog(Window owner, List<CustomerRequests.Response> customers,
			List<EmployeeRequests.Response> employees, List<ServiceRequests.Response> services) {
		this(owner, customers, employees, services, null);
	}

	public AppointmentDialog(Window owner, List<CustomerRequests.Response> customers,
			List<EmployeeRequests.Response> employees, List<ServiceRequests.Response> services,
			AppointmentRequests.Response editingAppointment) {
		super(owner, "Lịch hẹn", ModalityType.APPLICATION_MODAL);
		this.editingAppointment = editingAppointment;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(560, 560);
		setLocationRelativeTo(owner);
		setResizable(false);
		setContentPane(createContentPane(customers, employees, services));

		if (editingAppointment != null) {
			populateFormData(editingAppointment);
		}
	}

	private JPanel createContentPane(List<CustomerRequests.Response> customers,
			List<EmployeeRequests.Response> employees, List<ServiceRequests.Response> services) {
		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setBackground(UIManager.getColor("Panel.background"));
		contentPane.add(createFormPanel(customers, employees, services), BorderLayout.CENTER);
		contentPane.add(createButtonPanel(), BorderLayout.SOUTH);
		return contentPane;
	}

	private JPanel createFormPanel(List<CustomerRequests.Response> customers,
			List<EmployeeRequests.Response> employees, List<ServiceRequests.Response> services) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		panel.add(new JLabel("Khách hàng:"), gbc);

		cbCustomer = new JComboBox<>();
		for (CustomerRequests.Response customer : customers) {
			cbCustomer.addItem(new ComboBoxItem(customer.id(), customer.fullName()));
		}
		cbCustomer.setRenderer(new ComboBoxRenderer());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbCustomer, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		panel.add(new JLabel("Nhân viên:"), gbc);

		cbEmployee = new JComboBox<>();
		for (EmployeeRequests.Response employee : employees) {
			cbEmployee.addItem(new ComboBoxItem(employee.id(), employee.fullName()));
		}
		cbEmployee.setRenderer(new ComboBoxRenderer());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbEmployee, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		panel.add(new JLabel("Dịch vụ:"), gbc);

		serviceList = new JList<>();
		serviceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		serviceList.setVisibleRowCount(6);
		serviceList.setCellRenderer(new ComboBoxRenderer());
		serviceList.setListData(services.stream().map(s -> new ComboBoxItem(s.id(), s.name())).toArray(ComboBoxItem[]::new));
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(new JScrollPane(serviceList), gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(new JLabel("Ngày/Giờ:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(createDateTimePicker(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		panel.add(new JLabel("Trạng thái:"), gbc);

		cbStatus = new JComboBox<>(AppointmentStatus.values());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbStatus, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		panel.add(new JLabel("Ghi chú:"), gbc);

		taNote = new JTextArea(5, 30);
		taNote.setLineWrap(true);
		taNote.setWrapStyleWord(true);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		panel.add(new JScrollPane(taNote), gbc);

		return panel;
	}

	private JPanel createDateTimePicker() {
		JPanel panel = new JPanel(new BorderLayout(6, 0));
		panel.setOpaque(false);

		spDateTime = new JSpinner(new SpinnerDateModel(
				Date.from(defaultAppointmentTime().atZone(ZoneId.systemDefault()).toInstant()),
				null, null, Calendar.MINUTE));
		spDateTime.setEditor(new JSpinner.DateEditor(spDateTime, "dd/MM/yyyy HH:mm"));
		panel.add(spDateTime, BorderLayout.CENTER);

		JPanel quickButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		quickButtons.setOpaque(false);

		JButton nowButton = new JButton("Bây giờ");
		nowButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		nowButton.addActionListener(e -> spDateTime.setValue(new Date()));
		quickButtons.add(nowButton);

		JButton plus30Button = new JButton("+30 phút");
		plus30Button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		plus30Button.addActionListener(e -> spDateTime.setValue(Date.from(
				getSelectedAppointmentTime().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant())));
		quickButtons.add(plus30Button);

		panel.add(quickButtons, BorderLayout.EAST);
		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setOpaque(false);

		JButton btnSave = new JButton(editingAppointment != null ? "Cập nhật" : "Tạo mới");
		btnSave.addActionListener(e -> onSave());
		JButton btnCancel = new JButton("Hủy");
		btnCancel.addActionListener(e -> dispose());

		panel.add(btnSave);
		panel.add(btnCancel);
		return panel;
	}

	private void populateFormData(AppointmentRequests.Response apt) {
		selectComboItem(cbCustomer, apt.customerId());
		selectComboItem(cbEmployee, apt.employeeId());

		List<Integer> serviceIds = apt.services().stream().map(AppointmentRequests.AppointmentServiceResponse::serviceId).toList();
		int[] selectedIndexes = IntStream.range(0, serviceList.getModel().getSize())
				.filter(i -> serviceIds.contains(serviceList.getModel().getElementAt(i).getId()))
				.toArray();
		serviceList.setSelectedIndices(selectedIndexes);

		spDateTime.setValue(Date.from(apt.appointmentTime().atZone(ZoneId.systemDefault()).toInstant()));
		cbStatus.setSelectedItem(apt.status());
		if (apt.note() != null) {
			taNote.setText(apt.note());
		}
	}

	private void selectComboItem(JComboBox<ComboBoxItem> comboBox, Integer id) {
		for (int i = 0; i < comboBox.getItemCount(); i++) {
			ComboBoxItem item = comboBox.getItemAt(i);
			if (item.getId().equals(id)) {
				comboBox.setSelectedIndex(i);
				return;
			}
		}
	}

	private void onSave() {
		if (!validateForm()) {
			return;
		}
		approved = true;
		dispose();
	}

	private boolean validateForm() {
		if (cbCustomer.getSelectedIndex() < 0) {
			showError("Vui lòng chọn khách hàng");
			return false;
		}
		if (cbEmployee.getSelectedIndex() < 0) {
			showError("Vui lòng chọn nhân viên");
			return false;
		}
		if (serviceList.getSelectedIndices().length == 0) {
			showError("Vui lòng chọn ít nhất một dịch vụ");
			return false;
		}
		return true;
	}

	public AppointmentRequests.Create getAppointmentCreateRequest() {
		ComboBoxItem customer = (ComboBoxItem) cbCustomer.getSelectedItem();
		ComboBoxItem employee = (ComboBoxItem) cbEmployee.getSelectedItem();
		List<Integer> serviceIds = serviceList.getSelectedValuesList().stream().map(ComboBoxItem::getId).toList();
		return new AppointmentRequests.Create(
				customer.getId(),
				employee.getId(),
				serviceIds,
				getSelectedAppointmentTime(),
				(AppointmentStatus) cbStatus.getSelectedItem(),
				taNote.getText().trim());
	}

	public AppointmentRequests.Update getAppointmentUpdateRequest() {
		ComboBoxItem customer = (ComboBoxItem) cbCustomer.getSelectedItem();
		ComboBoxItem employee = (ComboBoxItem) cbEmployee.getSelectedItem();
		List<Integer> serviceIds = serviceList.getSelectedValuesList().stream().map(ComboBoxItem::getId).toList();
		return new AppointmentRequests.Update(
				customer.getId(),
				employee.getId(),
				serviceIds,
				getSelectedAppointmentTime(),
				(AppointmentStatus) cbStatus.getSelectedItem(),
				taNote.getText().trim());
	}

	public boolean isApproved() {
		return approved;
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
	}

	private LocalDateTime getSelectedAppointmentTime() {
		Date selectedDate = (Date) spDateTime.getValue();
		return LocalDateTime.ofInstant(selectedDate.toInstant(), ZoneId.systemDefault()).withSecond(0).withNano(0);
	}

	private LocalDateTime defaultAppointmentTime() {
		LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
		int minutesToAdd = 30 - (now.getMinute() % 30);
		if (minutesToAdd == 30) {
			minutesToAdd = 0;
		}
		return now.plusMinutes(minutesToAdd);
	}

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
	}

	private static class ComboBoxRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setText(value == null ? "" : value.toString());
			return this;
		}
	}
}
