package com.salonnbooking.ui.panel;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.client.ApiClient;

/**
 * ServicePanel - Quản lý dịch vụ salon
 */
public class ServicePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTextField tfName;
	private JTextField tfPrice;
	private JTextField tfDuration;
	private JTextArea taDescription;
	private JCheckBox cbIsActive;
	private JTable table;
	private DefaultTableModel tableModel;

	private Integer selectedServiceId = null;

	public ServicePanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(UIManager.getColor("Panel.background"));

		add(createHeaderPanel(), BorderLayout.NORTH);
		add(createMainPanel(), BorderLayout.CENTER);

		loadServices();
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JLabel titleLabel = new JLabel("Quản lý dịch vụ");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		panel.add(titleLabel, BorderLayout.WEST);

		return panel;
	}

	private JPanel createMainPanel() {
		JPanel main = new JPanel(new BorderLayout(10, 10));
		main.setOpaque(false);

		main.add(createFormPanel(), BorderLayout.NORTH);
		main.add(createTablePanel(), BorderLayout.CENTER);

		return main;
	}

	private JPanel createFormPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createTitledBorder("Thông tin dịch vụ"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addLabel(panel, "Tên dịch vụ:", 0, 0, gbc);
		tfName = new JTextField(20);
		panel.add(tfName, setPosition(gbc, 1, 0));

		addLabel(panel, "Giá (VND):", 2, 0, gbc);
		tfPrice = new JTextField(15);
		panel.add(tfPrice, setPosition(gbc, 3, 0));

		addLabel(panel, "Thời gian (phút):", 0, 1, gbc);
		tfDuration = new JTextField(20);
		panel.add(tfDuration, setPosition(gbc, 1, 1));

		cbIsActive = new JCheckBox("Kích hoạt");
		cbIsActive.setOpaque(false);
		gbc.gridx = 3;
		gbc.gridy = 1;
		panel.add(cbIsActive, gbc);

		addLabel(panel, "Mô tả:", 0, 2, gbc);
		taDescription = new JTextArea(3, 40);
		taDescription.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(taDescription);
		panel.add(scrollPane, setPosition(gbc, 1, 2));

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		btnPanel.setOpaque(false);

		btnPanel.add(createButton("Thêm", e -> addService()));
		btnPanel.add(createButton("Cập nhật", e -> updateService()));
		btnPanel.add(createButton("Xóa", e -> deleteService()));
		btnPanel.add(createButton("Xóa form", e -> clearForm()));

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		panel.add(btnPanel, gbc);

		return panel;
	}

	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createTitledBorder("Danh sách dịch vụ"));

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
		table.setRowHeight(25);

		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
				loadFormFromTable();
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
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
							"Error loading services: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
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
							"Service added successfully!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadServices();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Error adding service: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private void updateService() {
		if (selectedServiceId == null) {
			JOptionPane.showMessageDialog(this, "Please select a service to update",
					"Warning", JOptionPane.WARNING_MESSAGE);
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
							"Service updated successfully!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadServices();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Error updating service: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		worker.execute();
	}

	private void deleteService() {
		if (selectedServiceId == null) {
			JOptionPane.showMessageDialog(this, "Please select a service to delete",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to delete this service?",
				"Confirm Delete", JOptionPane.YES_NO_OPTION);

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
							"Service deleted successfully!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					clearForm();
					loadServices();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ServicePanel.this,
							"Error deleting service: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(this, "Service name is required", "Validation Error",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		try {
			new BigDecimal(tfPrice.getText().trim());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Price must be a valid number", "Validation Error",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		try {
			Integer.parseInt(tfDuration.getText().trim());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Duration must be a valid number",
					"Validation Error", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
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
