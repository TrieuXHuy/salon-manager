package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

/**
 * ServicePanel - Quản lý dịch vụ salon
 */
public class ServicePanel extends JPanel {
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

	private JTextField tfName;
	private JTextField tfPrice;
	private JTextField tfDuration;
	private JTextArea taDescription;
	private JCheckBox cbIsActive;
	private JTable table;
	private DefaultTableModel tableModel;

	private Integer selectedServiceId = null;

	public ServicePanel() {
		setLayout(new MigLayout("insets 24, fill, wrap 1", "[grow]", "[]18[grow]"));
		setBackground(BG_MAIN);

		add(createHeaderPanel(), "growx");
		add(createMainPanel(), "grow");

		loadServices();
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel titleLabel = new JLabel("Quản lý dịch vụ");
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(TEXT_MAIN);

		JLabel subtitle = new JLabel("Cập nhật danh mục và giá dịch vụ");
		subtitle.setFont(SUBTITLE_FONT);
		subtitle.setForeground(TEXT_MUTED);

		titleBlock.add(titleLabel);
		titleBlock.add(Box.createVerticalStrut(4));
		titleBlock.add(subtitle);
		panel.add(titleBlock, BorderLayout.WEST);

		return panel;
	}

	private JPanel createMainPanel() {
		JPanel main = new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow]", "[]18[grow]"));
		main.setOpaque(false);
		main.add(createFormPanel(), "growx");
		main.add(createTablePanel(), "grow");
		return main;
	}

	private JPanel createFormPanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));

		JLabel sectionTitle = new JLabel("Thông tin dịch vụ");
		sectionTitle.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 14)));
		sectionTitle.setForeground(TEXT_MAIN);
		panel.add(sectionTitle, BorderLayout.NORTH);

		JPanel fields = new JPanel(new GridBagLayout());
		fields.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addLabel(fields, "Tên dịch vụ:", 0, 0, gbc);
		tfName = new JTextField(20);
		styleField(tfName);
		fields.add(tfName, setPosition(gbc, 1, 0));

		addLabel(fields, "Giá (VND):", 2, 0, gbc);
		tfPrice = new JTextField(15);
		styleField(tfPrice);
		fields.add(tfPrice, setPosition(gbc, 3, 0));

		addLabel(fields, "Thời gian (phút):", 0, 1, gbc);
		tfDuration = new JTextField(20);
		styleField(tfDuration);
		fields.add(tfDuration, setPosition(gbc, 1, 1));

		cbIsActive = new JCheckBox("Kích hoạt");
		cbIsActive.setOpaque(false);
		cbIsActive.setFont(LABEL_FONT);
		cbIsActive.setForeground(TEXT_MUTED);
		gbc.gridx = 3;
		gbc.gridy = 1;
		fields.add(cbIsActive, gbc);

		addLabel(fields, "Mô tả:", 0, 2, gbc);
		taDescription = new JTextArea(3, 40);
		taDescription.setLineWrap(true);
		taDescription.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		JScrollPane scrollPane = new JScrollPane(taDescription);
		scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
		fields.add(scrollPane, setPosition(gbc, 1, 2));

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		btnPanel.setOpaque(false);

		btnPanel.add(createButton("Thêm", e -> addService()));
		btnPanel.add(createButton("Cập nhật", e -> updateService()));
		btnPanel.add(createButton("Xóa", e -> deleteService()));
		btnPanel.add(createButton("Xóa form", e -> clearForm()));

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		fields.add(btnPanel, gbc);

		panel.add(fields, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createTablePanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout(12, 12));
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));

		JLabel title = new JLabel("Danh sách dịch vụ");
		title.setFont(new Font("Segoe UI", Font.BOLD, 14));
		title.setForeground(TEXT_MAIN);
		panel.add(title, BorderLayout.NORTH);

		String[] columnNames = { "ID", "Tên", "Giá", "Thời gian (phút)", "Kích hoạt", "Mô tả" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(40);
		table.setFont(TABLE_FONT);
		table.setForeground(TEXT_MAIN);
		table.setShowVerticalLines(false);
		table.setShowHorizontalLines(true);
		table.setGridColor(BORDER);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setFillsViewportHeight(true);
		table.setBackground(Color.WHITE);
		table.setSelectionBackground(new Color(241, 245, 249));
		table.setSelectionForeground(TEXT_MAIN);

		// Custom Header Customization
		JTableHeader header = table.getTableHeader();
		header.setBackground(new Color(248, 250, 252));
		header.setForeground(TEXT_MUTED);
		header.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		header.setPreferredSize(new Dimension(100, 40));
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
		((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

		// Custom Header Renderer for padding
		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
				setBackground(new Color(248, 250, 252));
				setForeground(TEXT_MUTED);
				setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
				return this;
			}
		};
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}

		// Custom Cell Renderer for Row Styling & Padding
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
				if (!isSelected) {
					setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
				}
				return this;
			}
		};
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
		}

		// Fix column width
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(80);

		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
				loadFormFromTable();
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
		scrollPane.getViewport().setBackground(Color.WHITE);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private void loadServices() {
		SwingWorker<List<ServiceRequests.Response>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<ServiceRequests.Response> doInBackground() throws Exception {
				return ApiClient.getAllServices();
			}

			@Override
			protected void done() {
				try {
					List<ServiceRequests.Response> services = get();
					refreshTable(services);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Lỗi tải dịch vụ: " + e.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private void refreshTable(List<ServiceRequests.Response> services) {
		tableModel.setRowCount(0);

		for (ServiceRequests.Response service : services) {
			tableModel.addRow(new Object[] {
					service.id(),
					service.name(),
					service.price(),
					service.durationMinutes(),
					service.isActive(),
					service.description()
			});
		}
	}

	private void loadFormFromTable() {
		int row = table.getSelectedRow();
		if (row >= 0) {
			selectedServiceId = (Integer) tableModel.getValueAt(row, 0);
			tfName.setText((String) tableModel.getValueAt(row, 1));
			tfPrice.setText(tableModel.getValueAt(row, 2).toString());
			tfDuration.setText(tableModel.getValueAt(row, 3).toString());
			cbIsActive.setSelected((Boolean) tableModel.getValueAt(row, 4));
			taDescription.setText((String) tableModel.getValueAt(row, 5));
		}
	}

	private void addService() {
		if (!validateForm()) {
			return;
		}

		var createReq = new ServiceRequests.Create(
				tfName.getText().trim(),
				new BigDecimal(tfPrice.getText().trim()),
				Integer.parseInt(tfDuration.getText().trim()),
				taDescription.getText().trim(),
				cbIsActive.isSelected());

		SwingWorker<ServiceRequests.Response, Void> worker = new SwingWorker<>() {
			@Override
			protected ServiceRequests.Response doInBackground() throws Exception {
				return ApiClient.createService(createReq);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Thêm dịch vụ thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadServices();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Lỗi thêm dịch vụ: " + e.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private void updateService() {
		if (selectedServiceId == null) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ để cập nhật",
					"Cảnh báo", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!validateForm()) {
			return;
		}

		var updateReq = new ServiceRequests.Update(
				tfName.getText().trim(),
				new BigDecimal(tfPrice.getText().trim()),
				Integer.parseInt(tfDuration.getText().trim()),
				taDescription.getText().trim(),
				cbIsActive.isSelected());

		int serviceId = selectedServiceId;

		SwingWorker<ServiceRequests.Response, Void> worker = new SwingWorker<>() {
			@Override
			protected ServiceRequests.Response doInBackground() throws Exception {
				return ApiClient.updateService(serviceId, updateReq);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Cập nhật dịch vụ thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadServices();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Lỗi cập nhật dịch vụ: " + e.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private void deleteService() {
		if (selectedServiceId == null) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ để xóa",
					"Cảnh báo", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Bạn chắc chắn muốn xóa dịch vụ này?",
				"Xác nhận xóa", JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		int serviceId = selectedServiceId;

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				ApiClient.deleteService(serviceId);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Xóa dịch vụ thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadServices();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Lỗi xóa dịch vụ: " + e.getMessage(),
							"Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private void clearForm() {
		tfName.setText("");
		tfPrice.setText("");
		tfDuration.setText("");
		taDescription.setText("");
		cbIsActive.setSelected(false);
		selectedServiceId = null;
		table.clearSelection();
	}

	private boolean validateForm() {
		if (tfName.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Tên dịch vụ là bắt buộc", "Lỗi kiểm tra",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		try {
			new BigDecimal(tfPrice.getText().trim());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Giá phải là số hợp lệ", "Lỗi kiểm tra",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		try {
			Integer.parseInt(tfDuration.getText().trim());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Thời gian phải là số hợp lệ",
					"Lỗi kiểm tra", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

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
		btn.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		btn.setFocusPainted(false);
		btn.setBackground(PRIMARY_SOFT);
		btn.setForeground(PRIMARY);
		btn.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(PRIMARY_SOFT),
				new EmptyBorder(8, 16, 8, 16)));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.addActionListener(listener);
		return btn;
	}

	private void styleField(JTextField field) {
		field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(0, 10, 0, 10)));
		field.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13)));
		field.setPreferredSize(new Dimension(180, 36));
		field.setBackground(Color.WHITE);
		field.setForeground(TEXT_MAIN);
	}
}
