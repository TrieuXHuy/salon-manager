package com.salonnbooking.ui.panel;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.domain.Gender;

/**
 * CustomerPanel - Quản lý danh sách khách hàng
 * 
 * Chứa:
 * - Form input (Tên, SĐT, Email, Giới tính)
 * - JTable hiển thị danh sách
 * - Buttons: Thêm, Sửa, Xóa, Làm mới
 */
public class CustomerPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	// UI Components
	private JTextField tfFullName;
	private JTextField tfPhone;
	private JTextField tfEmail;
	private JComboBox<Gender> cbGender;
	private JTable table;
	private DefaultTableModel tableModel;

	private Integer selectedCustomerId = null;

	public CustomerPanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(UIManager.getColor("Panel.background"));

		// Header
		add(createHeaderPanel(), BorderLayout.NORTH);

		// Main Content
		add(createMainPanel(), BorderLayout.CENTER);

		// Load dữ liệu ban đầu
		loadCustomers();
	}

	/**
	 * Tạo panel header với tiêu đề
	 */
	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JLabel titleLabel = new JLabel("Quản lý khách hàng");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		panel.add(titleLabel, BorderLayout.WEST);

		return panel;
	}

	/**
	 * Tạo panel chính chứa form và table
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
		panel.setBorder(BorderFactory.createTitledBorder("Thông tin khách hàng"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Row 1: Full Name & Phone
		addLabel(panel, "Họ và tên:", 0, 0, gbc);
		tfFullName = new JTextField(20);
		panel.add(tfFullName, setPosition(gbc, 1, 0));

		addLabel(panel, "Số điện thoại:", 2, 0, gbc);
		tfPhone = new JTextField(15);
		panel.add(tfPhone, setPosition(gbc, 3, 0));

		// Row 2: Email & Gender
		addLabel(panel, "Email:", 0, 1, gbc);
		tfEmail = new JTextField(20);
		panel.add(tfEmail, setPosition(gbc, 1, 1));

		addLabel(panel, "Giới tính:", 2, 1, gbc);
		cbGender = new JComboBox<>(Gender.values());
		cbGender.setPreferredSize(new Dimension(100, 30));
		panel.add(cbGender, setPosition(gbc, 3, 1));

		// Buttons
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		btnPanel.setOpaque(false);

		JButton btnAdd = createButton("Thêm", e -> addCustomer());
		JButton btnUpdate = createButton("Cập nhật", e -> updateCustomer());
		JButton btnDelete = createButton("Xóa", e -> deleteCustomer());
		JButton btnClear = createButton("Xóa form", e -> clearForm());

		btnPanel.add(btnAdd);
		btnPanel.add(btnUpdate);
		btnPanel.add(btnDelete);
		btnPanel.add(btnClear);

		gbc.gridx = 0;
		gbc.gridy = 2;
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
		panel.setBorder(BorderFactory.createTitledBorder("Danh sách khách hàng"));

		// Tạo table model
		String[] columnNames = { "ID", "Họ và tên", "Số điện thoại", "Email", "Giới tính" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Table chỉ read-only
			}
		};

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(25);

		// Add row selection listener
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
	 * Load danh sách khách hàng từ API
	 */
	private void loadCustomers() {
		// Chạy trên background thread để không block EDT
		SwingWorker<List<CustomerRequests.Response>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<CustomerRequests.Response> doInBackground() throws Exception {
				return ApiClient.getAllCustomers();
			}

			@Override
			protected void done() {
				try {
					List<CustomerRequests.Response> customers = get();
					refreshTable(customers);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(CustomerPanel.this,
									"Lỗi tải khách hàng: " + e.getMessage(),
									"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Làm mới bảng từ danh sách customers
	 */
	private void refreshTable(List<CustomerRequests.Response> customers) {
		tableModel.setRowCount(0); // Clear old data

		for (CustomerRequests.Response customer : customers) {
			tableModel.addRow(new Object[] {
					customer.id(),
					customer.fullName(),
					customer.phone(),
					customer.email(),
					customer.gender()
			});
		}
	}

	/**
	 * Load form từ hàng được chọn trong bảng
	 */
	private void loadFormFromTable() {
		int row = table.getSelectedRow();
		if (row >= 0) {
			selectedCustomerId = (Integer) tableModel.getValueAt(row, 0);
			tfFullName.setText((String) tableModel.getValueAt(row, 1));
			tfPhone.setText((String) tableModel.getValueAt(row, 2));
			tfEmail.setText((String) tableModel.getValueAt(row, 3));
			cbGender.setSelectedItem(tableModel.getValueAt(row, 4));
		}
	}

	/**
	 * Thêm khách hàng mới
	 */
	private void addCustomer() {
		if (!validateForm()) {
			return;
		}

		var createReq = new CustomerRequests.Create(
				tfFullName.getText().trim(),
				tfPhone.getText().trim(),
				tfEmail.getText().trim(),
				(Gender) cbGender.getSelectedItem());

		SwingWorker<CustomerRequests.Response, Void> worker = new SwingWorker<>() {
			@Override
			protected CustomerRequests.Response doInBackground() throws Exception {
				return ApiClient.createCustomer(createReq);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(CustomerPanel.this,
									"Thêm khách hàng thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadCustomers();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(CustomerPanel.this,
									"Lỗi thêm khách hàng: " + e.getMessage(),
									"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Cập nhật khách hàng
	 */
	private void updateCustomer() {
		if (selectedCustomerId == null) {
		JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để cập nhật",
				"Cảnh báo", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!validateForm()) {
			return;
		}

		var updateReq = new CustomerRequests.Update(
				tfFullName.getText().trim(),
				tfPhone.getText().trim(),
				tfEmail.getText().trim(),
				(Gender) cbGender.getSelectedItem());

		int customerId = selectedCustomerId;

		SwingWorker<CustomerRequests.Response, Void> worker = new SwingWorker<>() {
			@Override
			protected CustomerRequests.Response doInBackground() throws Exception {
				return ApiClient.updateCustomer(customerId, updateReq);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(CustomerPanel.this,
									"Cập nhật khách hàng thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadCustomers();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(CustomerPanel.this,
									"Lỗi cập nhật khách hàng: " + e.getMessage(),
									"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Xóa khách hàng
	 */
	private void deleteCustomer() {
		if (selectedCustomerId == null) {
		JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để xóa",
				"Cảnh báo", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
"Bạn chắc chắn muốn xóa khách hàng này?",
			"Xác nhận xóa", JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		int customerId = selectedCustomerId;

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				ApiClient.deleteCustomer(customerId);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(CustomerPanel.this,
									"Xóa khách hàng thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadCustomers();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(CustomerPanel.this,
									"Lỗi xóa khách hàng: " + e.getMessage(),
									"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	/**
	 * Xóa nội dung form
	 */
	private void clearForm() {
		tfFullName.setText("");
		tfPhone.setText("");
		tfEmail.setText("");
		cbGender.setSelectedIndex(0);
		selectedCustomerId = null;
		table.clearSelection();
	}

	/**
	 * Kiểm tra tính hợp lệ của form
	 */
	private boolean validateForm() {
		if (tfFullName.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Họ và tên là bắt buộc", "Lỗi xác nhận",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (tfPhone.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Số điện thoại là bắt buộc", "Lỗi xác nhận",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (tfEmail.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Email là bắt buộc", "Lỗi xác nhận",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		// Validate email format
		if (!tfEmail.getText().contains("@")) {
			JOptionPane.showMessageDialog(this, "Định dạng email không hợp lệ", "Lỗi xác nhận",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	// ========== HELPER METHODS ==========

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
