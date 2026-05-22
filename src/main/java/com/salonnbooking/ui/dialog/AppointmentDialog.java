package com.salonnbooking.ui.dialog;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

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
	private JList<ComboBoxItem> lbService;
	private JSpinner spDateTime;
	private JComboBox<AppointmentStatus> cbStatus;
	private JTextArea taNote;
	private JButton btnSave;
	private JButton btnCancel;

	private boolean approved = false;
	private AppointmentRequests.Response editingAppointment = null;
	private Supplier<List<CustomerRequests.Response>> customerLoader; // Callback để load customers từ API
	private Supplier<List<ServiceRequests.Response>> serviceLoader; // Callback để load services từ API

	/**
	 * Constructor - Tạo dialog (Add mode)
	 */
	public AppointmentDialog(Window owner, 
			Supplier<List<CustomerRequests.Response>> customerLoader,
			Supplier<List<ServiceRequests.Response>> serviceLoader) {
		this(owner, customerLoader, serviceLoader, null);
	}

	/**
	 * Constructor - Tạo dialog (Edit mode)
	 */
	public AppointmentDialog(Window owner, 
			Supplier<List<CustomerRequests.Response>> customerLoader,
			Supplier<List<ServiceRequests.Response>> serviceLoader,
			AppointmentRequests.Response editingAppointment) {
		super(owner, "Lịch hẹn", ModalityType.APPLICATION_MODAL);
		this.editingAppointment = editingAppointment;
		this.customerLoader = customerLoader;
		this.serviceLoader = serviceLoader;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(500, 550);
		setLocationRelativeTo(owner);
		setResizable(false);

		JPanel contentPane = createContentPane(editingAppointment);
		setContentPane(contentPane);

		// Populate data if editing
		if (editingAppointment != null) {
			populateFormData(editingAppointment);
		}
	}

	/**
	 * Tạo content panel
	 */
	private JPanel createContentPane(AppointmentRequests.Response editingAppointment) {
		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		contentPane.setBackground(UIManager.getColor("Panel.background"));

		// Form Panel
		contentPane.add(createFormPanel(), BorderLayout.CENTER);

		// Button Panel
		contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

		return contentPane;
	}

	/**
	 * Tạo form input panel
	 */
	private JPanel createFormPanel() {
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
		cbCustomer.setRenderer(new ComboBoxRenderer());
		
		// Thêm PopupMenuListener để load customers từ API khi mở dropdown
		cbCustomer.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// Load customers từ API khi mở dropdown
				loadCustomersFromApi();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbCustomer, gbc);

		// Service List (Multi-select)
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		panel.add(new JLabel("Dịch vụ:"), gbc);

		DefaultListModel<ComboBoxItem> serviceModel = new DefaultListModel<>();
		lbService = new JList<>(serviceModel);
		lbService.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lbService.setCellRenderer(new ListCellRenderer<ComboBoxItem>() {
			private final JLabel label = new JLabel();

			@Override
			public Component getListCellRendererComponent(JList<? extends ComboBoxItem> list, ComboBoxItem value,
					int index, boolean isSelected, boolean cellHasFocus) {
				if (value != null) {
					label.setText(value.toString());
				}
				if (isSelected) {
					label.setBackground(list.getSelectionBackground());
					label.setForeground(list.getSelectionForeground());
				} else {
					label.setBackground(list.getBackground());
					label.setForeground(list.getForeground());
				}
				label.setOpaque(true);
				return label;
			}
		});

		JScrollPane serviceScrollPane = new JScrollPane(lbService);
		serviceScrollPane.setPreferredSize(new Dimension(200, 80));
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.3;
		gbc.anchor = GridBagConstraints.NORTH;
		panel.add(serviceScrollPane, gbc);

		// Tạo thread để load services lần đầu
		SwingUtilities.invokeLater(this::loadServicesFromApi);

		// DateTime TextField
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.BASELINE;
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
		cbStatus.setRenderer(new StatusComboBoxRenderer());
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(cbStatus, gbc);

		// Note TextArea
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		panel.add(new JLabel("Ghi chú:"), gbc);

		taNote = new JTextArea(4, 30);
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
	 * Load customers từ API và update combo box
	 */
	private void loadCustomersFromApi() {
		try {
			List<CustomerRequests.Response> customers = customerLoader.get();
			
			// Lưu customer hiện tại được chọn (nếu có)
			ComboBoxItem selectedItem = (ComboBoxItem) cbCustomer.getSelectedItem();
			Integer selectedId = selectedItem != null ? selectedItem.getId() : null;
			
			// Clear và reload combo box
			cbCustomer.removeAllItems();
			for (CustomerRequests.Response c : customers) {
				cbCustomer.addItem(new ComboBoxItem(c.id(), c.fullName()));
			}
			
			// Restore previous selection nếu still exists
			if (selectedId != null) {
				for (int i = 0; i < cbCustomer.getItemCount(); i++) {
					ComboBoxItem item = cbCustomer.getItemAt(i);
					if (item.getId().equals(selectedId)) {
						cbCustomer.setSelectedIndex(i);
						break;
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, 
				"Lỗi tải danh sách khách hàng: " + e.getMessage(),
				"Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Load services từ API và update list
	 */
	private void loadServicesFromApi() {
		try {
			List<ServiceRequests.Response> services = serviceLoader.get();
			
			// Lưu các service hiện tại được chọn (nếu có)
			int[] selectedIndices = lbService.getSelectedIndices();
			List<Integer> selectedIds = new java.util.ArrayList<>();
			if (selectedIndices != null) {
				for (int i : selectedIndices) {
					if (i < lbService.getModel().getSize()) {
						selectedIds.add(((ComboBoxItem) lbService.getModel().getElementAt(i)).getId());
					}
				}
			}
			
			// Clear và reload list
			DefaultListModel<ComboBoxItem> model = (DefaultListModel<ComboBoxItem>) lbService.getModel();
			model.clear();
			for (ServiceRequests.Response s : services) {
				model.addElement(new ComboBoxItem(s.id(), s.name()));
			}
			
			// Restore previous selection nếu still exists
			if (!selectedIds.isEmpty()) {
				java.util.List<Integer> indiciesToSelect = new java.util.ArrayList<>();
				for (int i = 0; i < model.getSize(); i++) {
					ComboBoxItem item = model.getElementAt(i);
					if (selectedIds.contains(item.getId())) {
						indiciesToSelect.add(i);
					}
				}
				if (!indiciesToSelect.isEmpty()) {
					int[] indices = indiciesToSelect.stream().mapToInt(Integer::intValue).toArray();
					lbService.setSelectedIndices(indices);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, 
				"Lỗi tải danh sách dịch vụ: " + e.getMessage(),
				"Lỗi", JOptionPane.ERROR_MESSAGE);
		}
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
	private void populateFormData(AppointmentRequests.Response apt) {
		// Load customers từ API để đảm bảo có customer mà đang edit
		try {
			List<CustomerRequests.Response> customers = customerLoader.get();
			cbCustomer.removeAllItems();
			for (CustomerRequests.Response c : customers) {
				cbCustomer.addItem(new ComboBoxItem(c.id(), c.fullName()));
			}
			
			// Set customer
			for (int i = 0; i < cbCustomer.getItemCount(); i++) {
				ComboBoxItem item = cbCustomer.getItemAt(i);
				if (item.getId().equals(apt.customerId())) {
					cbCustomer.setSelectedIndex(i);
					break;
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, 
				"Lỗi tải danh sách khách hàng: " + e.getMessage(),
				"Lỗi", JOptionPane.ERROR_MESSAGE);
		}

		// Load services từ API để đảm bảo có services mà đang edit
		try {
			List<ServiceRequests.Response> services = serviceLoader.get();
			DefaultListModel<ComboBoxItem> model = (DefaultListModel<ComboBoxItem>) lbService.getModel();
			model.clear();
			for (ServiceRequests.Response s : services) {
				model.addElement(new ComboBoxItem(s.id(), s.name()));
			}
			
			// Set services
			if (apt.serviceIds() != null && !apt.serviceIds().isEmpty()) {
				java.util.List<Integer> indiciesToSelect = new java.util.ArrayList<>();
				for (int i = 0; i < model.getSize(); i++) {
					ComboBoxItem item = model.getElementAt(i);
					if (apt.serviceIds().contains(item.getId())) {
						indiciesToSelect.add(i);
					}
				}
				if (!indiciesToSelect.isEmpty()) {
					int[] indices = indiciesToSelect.stream().mapToInt(Integer::intValue).toArray();
					lbService.setSelectedIndices(indices);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, 
				"Lỗi tải danh sách dịch vụ: " + e.getMessage(),
				"Lỗi", JOptionPane.ERROR_MESSAGE);
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

		if (lbService.getSelectedIndices() == null || lbService.getSelectedIndices().length == 0) {
			showError("Vui lòng chọn ít nhất một dịch vụ");
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
		LocalDateTime dateTime = getSelectedAppointmentTime();
		
		// Get selected service IDs
		List<Integer> serviceIds = new java.util.ArrayList<>();
		for (int index : lbService.getSelectedIndices()) {
			serviceIds.add(((ComboBoxItem) lbService.getModel().getElementAt(index)).getId());
		}

		return new AppointmentRequests.Create(
				customer.getId(),
				serviceIds,
				dateTime,
				(AppointmentStatus) cbStatus.getSelectedItem(),
				taNote.getText().trim());
	}

	/**
	 * Get appointment update request
	 */
	public AppointmentRequests.Update getAppointmentUpdateRequest() {
		ComboBoxItem customer = (ComboBoxItem) cbCustomer.getSelectedItem();
		LocalDateTime dateTime = getSelectedAppointmentTime();
		
		// Get selected service IDs
		List<Integer> serviceIds = new java.util.ArrayList<>();
		for (int index : lbService.getSelectedIndices()) {
			serviceIds.add(((ComboBoxItem) lbService.getModel().getElementAt(index)).getId());
		}

		return new AppointmentRequests.Update(
				customer.getId(),
				serviceIds,
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

	private static class StatusComboBoxRenderer extends JLabel implements javax.swing.ListCellRenderer<AppointmentStatus> {
		private static final long serialVersionUID = 1L;

		public StatusComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends AppointmentStatus> list, AppointmentStatus value,
				int index, boolean isSelected, boolean cellHasFocus) {
			setText(value != null ? value.getDisplayName() : "");

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
