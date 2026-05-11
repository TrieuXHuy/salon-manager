package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

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
	private static final Color BG_MAIN = new Color(248, 250, 252);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);
	private static final Color BORDER = new Color(226, 232, 240);
	private static final Color PRIMARY = new Color(109, 73, 224);
	private static final Color PRIMARY_SOFT = new Color(237, 233, 255);
	private static final Font TITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 24));
	private static final Font SUBTITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12));
	private static final Font LABEL_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12));
	private static final Font TABLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13));

	// UI Components
	private JTextField tfFullName;
	private JTextField tfPhone;
	private JTextField tfEmail;
	private JComboBox<Gender> cbGender;
	private JTable table;
	private DefaultTableModel tableModel;

	private Integer selectedCustomerId = null;

	public CustomerPanel() {
		setLayout(new MigLayout("insets 24, fill, wrap 1", "[grow]", "[]18[grow]"));
		setBackground(BG_MAIN);

		add(createHeaderPanel(), "growx");
		add(createMainPanel(), "grow");

		// Load dữ liệu ban đầu
		loadCustomers();
	}

	/**
	 * Tạo panel header với tiêu đề
	 */
	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel titleLabel = new JLabel("Quản lý khách hàng");
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(TEXT_MAIN);

		JLabel subtitle = new JLabel("Theo dõi & cập nhật hồ sơ khách hàng");
		subtitle.setFont(SUBTITLE_FONT);
		subtitle.setForeground(TEXT_MUTED);

		titleBlock.add(titleLabel);
		titleBlock.add(Box.createVerticalStrut(4));
		titleBlock.add(subtitle);
		panel.add(titleBlock, BorderLayout.WEST);

		return panel;
	}

	/**
	 * Tạo panel chính chứa form và table
	 */
	private JPanel createMainPanel() {
		JPanel main = new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow]", "[]18[grow]"));
		main.setOpaque(false);
		main.add(createFormPanel(), "growx");
		main.add(createTablePanel(), "grow");
		return main;
	}

	/**
	 * Tạo form input
	 */
	private JPanel createFormPanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));

		JLabel sectionTitle = new JLabel("Thông tin khách hàng");
		sectionTitle.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 14)));
		sectionTitle.setForeground(TEXT_MAIN);
		panel.add(sectionTitle, BorderLayout.NORTH);

		JPanel fields = new JPanel(new GridBagLayout());
		fields.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Row 1: Full Name & Phone
		addLabel(fields, "Họ và tên:", 0, 0, gbc);
		tfFullName = new JTextField(20);
		styleField(tfFullName);
		fields.add(tfFullName, setPosition(gbc, 1, 0));

		addLabel(fields, "Số điện thoại:", 2, 0, gbc);
		tfPhone = new JTextField(15);
		styleField(tfPhone);
		fields.add(tfPhone, setPosition(gbc, 3, 0));

		// Row 2: Email & Gender
		addLabel(fields, "Email:", 0, 1, gbc);
		tfEmail = new JTextField(20);
		styleField(tfEmail);
		fields.add(tfEmail, setPosition(gbc, 1, 1));

		addLabel(fields, "Giới tính:", 2, 1, gbc);
		cbGender = new JComboBox<>(Gender.values());
		cbGender.setRenderer(createGenderRenderer());
		cbGender.setPreferredSize(new Dimension(100, 32));
		fields.add(cbGender, setPosition(gbc, 3, 1));

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
		fields.add(btnPanel, gbc);

		panel.add(fields, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Tạo panel bảng danh sách
	 */
	private JPanel createTablePanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout(12, 12));
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));

		JLabel title = new JLabel("Danh sách khách hàng");
		title.setFont(new Font("Segoe UI", Font.BOLD, 14));
		title.setForeground(TEXT_MAIN);
		panel.add(title, BorderLayout.NORTH);

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
		table.setRowHeight(32);
		table.setFont(TABLE_FONT);
		table.setForeground(TEXT_MAIN);
		table.setShowVerticalLines(false);
		table.setGridColor(BORDER);
		table.getTableHeader().setBackground(new Color(245, 243, 255));
		table.getTableHeader().setForeground(TEXT_MUTED);
		table.getTableHeader().setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));

		// Add row selection listener
		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
				loadFormFromTable();
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
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
					customer.gender() == null ? "" : customer.gender().getDisplayName()
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
			cbGender.setSelectedItem(parseGender(tableModel.getValueAt(row, 4)));
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
		JLabel label = new JLabel(text);
		label.setFont(LABEL_FONT);
		label.setForeground(TEXT_MUTED);
		panel.add(label, gbc);
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
		field.setPreferredSize(new Dimension(180, 32));
		field.setBackground(Color.WHITE);
		field.setForeground(TEXT_MAIN);
	}

	private Gender parseGender(Object value) {
		if (value == null) {
			return Gender.other;
		}
		String text = value.toString();
		for (Gender gender : Gender.values()) {
			if (gender.name().equalsIgnoreCase(text) || gender.getDisplayName().equalsIgnoreCase(text)) {
				return gender;
			}
		}
		return Gender.other;
	}

	private ListCellRenderer<? super Gender> createGenderRenderer() {
		return (list, value, index, isSelected, cellHasFocus) -> {
			JLabel label = new JLabel(value == null ? "" : value.getDisplayName());
			label.setOpaque(true);
			label.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
			label.setBorder(new EmptyBorder(4, 8, 4, 8));
			label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			return label;
		};
	}
}
