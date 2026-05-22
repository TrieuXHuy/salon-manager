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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.salonnbooking.api.dto.AuthRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.domain.UserRole;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;

import net.miginfocom.swing.MigLayout;

public class UserPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final Color BG_MAIN = new Color(248, 250, 252);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);
	private static final Color BORDER = new Color(226, 232, 240);
	private static final Color PRIMARY = new Color(109, 73, 224);
	private static final Color PRIMARY_SOFT = new Color(237, 233, 255);

	private final String requesterUsername;
	private JTextField tfUsername;
	private JPasswordField tfPassword;
	private JComboBox<UserRole> cbRole;
	private JTable table;
	private DefaultTableModel tableModel;
	private Integer selectedUserId;

	public UserPanel(String requesterUsername) {
		this.requesterUsername = requesterUsername;
		setLayout(new MigLayout("insets 24, fill, wrap 1", "[grow]", "[]18[grow]"));
		setBackground(BG_MAIN);
		add(createHeaderPanel(), "growx");
		add(createMainPanel(), "grow");
		loadUsers();
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel titleLabel = new JLabel("Quan ly tai khoan");
		titleLabel.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 24)));
		titleLabel.setForeground(TEXT_MAIN);
		JLabel subtitle = new JLabel("Tao tai khoan va phan quyen nguoi dung");
		subtitle.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
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

		JLabel sectionTitle = new JLabel("Thong tin tai khoan");
		sectionTitle.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 14)));
		sectionTitle.setForeground(TEXT_MAIN);
		panel.add(sectionTitle, BorderLayout.NORTH);

		JPanel fields = new JPanel(new GridBagLayout());
		fields.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addLabel(fields, "Username:", 0, 0, gbc);
		tfUsername = new JTextField(20);
		styleField(tfUsername);
		fields.add(tfUsername, setPosition(gbc, 1, 0));

		addLabel(fields, "Password:", 2, 0, gbc);
		tfPassword = new JPasswordField(20);
		styleField(tfPassword);
		fields.add(tfPassword, setPosition(gbc, 3, 0));

		addLabel(fields, "Vai tro:", 0, 1, gbc);
		cbRole = new JComboBox<>(UserRole.values());
		cbRole.setPreferredSize(new Dimension(180, 36));
		fields.add(cbRole, setPosition(gbc, 1, 1));

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		btnPanel.setOpaque(false);
		btnPanel.add(createButton("Tao tai khoan", e -> createUser()));
		btnPanel.add(createButton("Cap nhat vai tro", e -> updateRole()));
		btnPanel.add(createButton("Xoa form", e -> clearForm()));

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		fields.add(btnPanel, gbc);

		panel.add(fields, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createTablePanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout(12, 12));
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));

		JLabel title = new JLabel("Danh sach tai khoan");
		title.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 14)));
		title.setForeground(TEXT_MAIN);
		panel.add(title, BorderLayout.NORTH);

		tableModel = new DefaultTableModel(new String[] { "ID", "Username", "Vai tro" }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(40);
		table.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13)));
		table.setForeground(TEXT_MAIN);
		table.setShowVerticalLines(false);
		table.setGridColor(BORDER);
		table.setFillsViewportHeight(true);
		table.setSelectionBackground(new Color(241, 245, 249));
		table.setSelectionForeground(TEXT_MAIN);

		JTableHeader header = table.getTableHeader();
		header.setBackground(new Color(248, 250, 252));
		header.setForeground(TEXT_MUTED);
		header.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		header.setPreferredSize(new Dimension(100, 40));
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
				if (!isSelected) {
					setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
				}
				return this;
			}
		};
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(0).setMaxWidth(90);

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

	private void loadUsers() {
		SwingWorker<List<AuthRequests.UserResponse>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<AuthRequests.UserResponse> doInBackground() throws Exception {
				return ApiClient.getUsers(requesterUsername);
			}

			@Override
			protected void done() {
				try {
					refreshTable(get());
				} catch (Exception e) {
					showError("Loi tai danh sach tai khoan", e);
				}
			}
		};
		worker.execute();
	}

	private void refreshTable(List<AuthRequests.UserResponse> users) {
		tableModel.setRowCount(0);
		for (AuthRequests.UserResponse user : users) {
			tableModel.addRow(new Object[] { user.id(), user.username(), user.role() });
		}
	}

	private void loadFormFromTable() {
		int row = table.getSelectedRow();
		selectedUserId = (Integer) tableModel.getValueAt(row, 0);
		tfUsername.setText((String) tableModel.getValueAt(row, 1));
		tfPassword.setText("");
		cbRole.setSelectedItem(tableModel.getValueAt(row, 2));
	}

	private void createUser() {
		String username = tfUsername.getText().trim();
		String password = new String(tfPassword.getPassword());
		UserRole role = (UserRole) cbRole.getSelectedItem();
		if (username.isBlank() || password.isBlank() || role == null) {
			JOptionPane.showMessageDialog(this, "Username, password va vai tro la bat buoc.",
					"Validation", JOptionPane.WARNING_MESSAGE);
			return;
		}

		SwingWorker<AuthRequests.UserResponse, Void> worker = new SwingWorker<>() {
			@Override
			protected AuthRequests.UserResponse doInBackground() throws Exception {
				return ApiClient.createUser(requesterUsername, username, password, role);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(UserPanel.this, "Tao tai khoan thanh cong.");
					clearForm();
					loadUsers();
				} catch (Exception e) {
					showError("Loi tao tai khoan", e);
				}
			}
		};
		worker.execute();
	}

	private void updateRole() {
		if (selectedUserId == null) {
			JOptionPane.showMessageDialog(this, "Vui long chon tai khoan can cap nhat.",
					"Validation", JOptionPane.WARNING_MESSAGE);
			return;
		}
		UserRole role = (UserRole) cbRole.getSelectedItem();
		SwingWorker<AuthRequests.UserResponse, Void> worker = new SwingWorker<>() {
			@Override
			protected AuthRequests.UserResponse doInBackground() throws Exception {
				return ApiClient.changeUserRole(selectedUserId, requesterUsername, role);
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(UserPanel.this, "Cap nhat vai tro thanh cong.");
					clearForm();
					loadUsers();
				} catch (Exception e) {
					showError("Loi cap nhat vai tro", e);
				}
			}
		};
		worker.execute();
	}

	private void clearForm() {
		tfUsername.setText("");
		tfPassword.setText("");
		cbRole.setSelectedItem(UserRole.CUSTOMER);
		selectedUserId = null;
		table.clearSelection();
	}

	private void addLabel(JPanel panel, String text, int x, int y, GridBagConstraints gbc) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		JLabel label = new JLabel(text);
		label.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
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

	private void showError(String title, Exception e) {
		Throwable cause = e.getCause() != null ? e.getCause() : e;
		JOptionPane.showMessageDialog(this, title + ": " + cause.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
	}
}
