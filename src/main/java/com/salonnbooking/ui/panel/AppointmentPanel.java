package com.salonnbooking.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.PaymentRequests;
import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.ui.dialog.AppointmentDialog;
import com.salonnbooking.ui.dialog.PaymentSimulationDialog;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

/**
 * AppointmentPanel - Quản lý lịch hẹn
 * 
 * Tích hợp SwingWorker để gọi API không làm block EDT (Non-blocking UI)
 * Sử dụng AppointmentDialog cho form nhập liệu
 * Xử lý Exception & User Feedback toàn diện
 */
public class AppointmentPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final Color BG_MAIN = new Color(248, 250, 252);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);
	private static final Color BORDER = new Color(226, 232, 240);
	private static final Color PRIMARY = new Color(109, 73, 224);
	private static final Color PRIMARY_SOFT = new Color(237, 233, 255);
	private static final Font TITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 24));
	private static final Font SUBTITLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12));
	private static final Font TABLE_FONT = Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13));

	// UI Components
	private JButton btnAdd;
	private JButton btnEdit;
	private JButton btnDelete;
	private JButton btnPay;
	private JButton btnRefresh;
	private JTable table;
	private DefaultTableModel tableModel;
	private TableRowSorter<DefaultTableModel> tableSorter;
	private JTextField txtCustomerSearch;
	private JComboBox<String> cbServiceSearch;
	private JSpinner spDateTimeSearch;
	private JComboBox<String> cbStatusSearch;
	private JLabel lblStatus;
	private boolean dateTimeFilterActive = false;

	// Data
	private Integer selectedAppointmentId = null;
	private List<CustomerRequests.Response> customers;
	private List<ServiceRequests.Response> services;
	private List<AppointmentRequests.Response> appointments;

	public AppointmentPanel() {
		setLayout(new MigLayout("insets 24, fill, wrap 1", "[grow]", "[]18[grow]18[]"));
		setBackground(BG_MAIN);

		add(createHeaderPanel(), "growx");
		add(createTablePanel(), "grow");
		add(createToolbarPanel(), "growx");

		// Load dữ liệu ban đầu
		loadInitialData();
	}

	/**
	 * Tạo panel header
	 */
	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		JPanel titleBlock = new JPanel();
		titleBlock.setOpaque(false);
		titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

		JLabel titleLabel = new JLabel("Đặt lịch hẹn");
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(TEXT_MAIN);

		JLabel subtitle = new JLabel("Quản lý lịch hẹn và thanh toán nhanh");
		subtitle.setFont(SUBTITLE_FONT);
		subtitle.setForeground(TEXT_MUTED);

		titleBlock.add(titleLabel);
		titleBlock.add(Box.createVerticalStrut(4));
		titleBlock.add(subtitle);
		panel.add(titleBlock, BorderLayout.WEST);

		return panel;
	}

	/**
	 * Tạo toolbar với các button
	 */
	private JPanel createToolbarPanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(12, 12, 12, 12));

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		btnPanel.setOpaque(false);

		btnAdd = createButton("Thêm", e -> onAddButtonClicked());
		btnEdit = createButton("Sửa", e -> onEditButtonClicked());
		btnDelete = createButton("Xóa", e -> onDeleteButtonClicked());
		btnRefresh = createButton("Làm mới", e -> loadAppointments());

		btnAdd.setEnabled(false);
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnPay = createButton("Thanh toán", e -> onPayButtonClicked());
		btnPay.setEnabled(false);

		btnPanel.add(btnAdd);
		btnPanel.add(btnEdit);
		btnPanel.add(btnDelete);
		btnPanel.add(btnPay);
		btnPanel.add(btnRefresh);

		panel.add(btnPanel, BorderLayout.WEST);

		// Status label
		lblStatus = new JLabel("Sẵn sàng");
		lblStatus.setFont(SUBTITLE_FONT);
		lblStatus.setForeground(TEXT_MUTED);
		panel.add(lblStatus, BorderLayout.EAST);

		return panel;
	}

	/**
	 * Tạo panel bảng danh sách
	 */
	private JPanel createTablePanel() {
		RoundedPanel panel = new RoundedPanel(16, BG_CARD, true);
		panel.setLayout(new BorderLayout(12, 12));
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));

		JLabel title = new JLabel("Danh sách lịch hẹn");
		title.setFont(new Font("Segoe UI", Font.BOLD, 14));
		title.setForeground(TEXT_MAIN);
		panel.add(createTableHeaderPanel(title), BorderLayout.NORTH);

		String[] columnNames = { "ID", "Khách hàng", "Dịch vụ", "Ngày giờ", "Trạng thái", "Ghi chú" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Read-only table
			}
		};

		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(32);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoCreateRowSorter(true);
		table.setFont(TABLE_FONT);
		table.setForeground(TEXT_MAIN);
		table.setShowVerticalLines(false);
		table.setGridColor(BORDER);
		table.getTableHeader().setBackground(new Color(245, 243, 255));
		table.getTableHeader().setForeground(TEXT_MUTED);
		table.getTableHeader().setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		tableSorter = new TableRowSorter<>(tableModel);
		tableSorter.setComparator(3, Comparator.comparing(value -> LocalDateTime.parse(value.toString(), DATE_FORMATTER)));
		table.setRowSorter(tableSorter);

		// Row selection listener
		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				selectedAppointmentId = (Integer) tableModel.getValueAt(modelRow, 0);
				btnEdit.setEnabled(true);
				btnDelete.setEnabled(true);
				btnPay.setEnabled(true);
			} else {
				selectedAppointmentId = null;
				btnEdit.setEnabled(false);
				btnDelete.setEnabled(false);
				btnPay.setEnabled(false);
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createTableHeaderPanel(JLabel title) {
		JPanel header = new JPanel(new BorderLayout(0, 10));
		header.setOpaque(false);
		header.add(title, BorderLayout.NORTH);

		JPanel filters = new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[grow][grow]", "[][]"));
		filters.setOpaque(false);

		txtCustomerSearch = createSearchField("T\u00ecm kh\u00e1ch h\u00e0ng");
		cbServiceSearch = new JComboBox<>();
		cbServiceSearch.setFont(TABLE_FONT);
		cbServiceSearch.addItem("T\u1ea5t c\u1ea3 d\u1ecbch v\u1ee5");
		cbServiceSearch.addActionListener(e -> applyAppointmentFilters());
		cbStatusSearch = new JComboBox<>(new String[] {
				"T\u1ea5t c\u1ea3 tr\u1ea1ng th\u00e1i",
				"pending",
				"confirmed",
				"completed",
				"cancelled"
		});
		cbStatusSearch.setFont(TABLE_FONT);
		cbStatusSearch.addActionListener(e -> applyAppointmentFilters());

		filters.add(createFilterGroup("Kh\u00e1ch h\u00e0ng", txtCustomerSearch), "growx");
		filters.add(createFilterGroup("D\u1ecbch v\u1ee5", cbServiceSearch), "growx");
		filters.add(createFilterGroup("Ng\u00e0y gi\u1edd", createDateTimeFilter()), "growx");
		filters.add(createFilterGroup("Tr\u1ea1ng th\u00e1i", cbStatusSearch), "growx");
		header.add(filters, BorderLayout.CENTER);

		return header;
	}

	private JPanel createDateTimeFilter() {
		JPanel panel = new JPanel(new BorderLayout(6, 0));
		panel.setOpaque(false);

		spDateTimeSearch = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
		spDateTimeSearch.setEditor(new JSpinner.DateEditor(spDateTimeSearch, "dd/MM/yyyy HH:mm"));
		spDateTimeSearch.setFont(TABLE_FONT);
		spDateTimeSearch.setToolTipText("Ch\u1ecdn ng\u00e0y gi\u1edd \u0111\u1ec3 l\u1ecdc l\u1ecbch h\u1eb9n");
		spDateTimeSearch.addChangeListener(e -> {
			if (dateTimeFilterActive) {
				applyAppointmentFilters();
			}
		});
		panel.add(spDateTimeSearch, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttons.setOpaque(false);
		JButton btnApply = createSmallFilterButton("L\u1ecdc");
		btnApply.addActionListener(e -> {
			dateTimeFilterActive = true;
			applyAppointmentFilters();
		});
		JButton btnClear = createSmallFilterButton("X\u00f3a");
		btnClear.addActionListener(e -> {
			dateTimeFilterActive = false;
			applyAppointmentFilters();
		});
		buttons.add(btnApply);
		buttons.add(btnClear);
		panel.add(buttons, BorderLayout.EAST);

		return panel;
	}

	private JButton createSmallFilterButton(String label) {
		JButton button = new JButton(label);
		button.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 11)));
		button.setFocusPainted(false);
		button.setBackground(PRIMARY_SOFT);
		button.setForeground(PRIMARY);
		button.setBorder(new EmptyBorder(5, 10, 5, 10));
		return button;
	}

	private JPanel createFilterGroup(String label, java.awt.Component input) {
		JPanel group = new JPanel(new BorderLayout(0, 4));
		group.setOpaque(false);

		JLabel lbl = new JLabel(label);
		lbl.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 11)));
		lbl.setForeground(TEXT_MUTED);

		group.add(lbl, BorderLayout.NORTH);
		group.add(input, BorderLayout.CENTER);
		return group;
	}

	private JTextField createSearchField(String placeholder) {
		JTextField field = new JTextField();
		field.setToolTipText(placeholder);
		field.setFont(TABLE_FONT);
		field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				new EmptyBorder(6, 8, 6, 8)));
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				applyAppointmentFilters();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				applyAppointmentFilters();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				applyAppointmentFilters();
			}
		});
		return field;
	}

	// ========== DATA LOADING METHODS ==========

	/**
	 * Load dữ liệu ban đầu (Customers, Services, Appointments)
	 */
	private void loadInitialData() {
		setStatus("Đang tải dữ liệu...");
		disableButtons();

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
					populateServiceFilter();
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi tải dữ liệu ban đầu", e);
					setStatus("Lỗi");
					enableButtons();
				}
			}
		};

		worker.execute();
	}

	/**
	 * Load danh sách lịch hẹn từ API (Non-blocking)
	 */
	private void loadAppointments() {
		setStatus("Đang tải lịch hẹn...");

		SwingWorker<List<AppointmentRequests.Response>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<AppointmentRequests.Response> doInBackground() throws Exception {
				return ApiClient.getAllAppointments();
			}

			@Override
			protected void done() {
				try {
					appointments = sortByAppointmentTime(get());
					refreshTable(appointments);
					setStatus("Sẵn sàng - " + appointments.size() + " lịch hẹn");
					enableButtons();
				} catch (Exception e) {
					handleException("Lỗi tải lịch hẹn", e);
					setStatus("Lỗi tải dữ liệu");
				}
			}
		};

		worker.execute();
	}

	/**
	 * Làm mới bảng từ danh sách appointments
	 */
	private void refreshTable(List<AppointmentRequests.Response> appointments) {
		tableModel.setRowCount(0);

		for (AppointmentRequests.Response apt : appointments) {
			String customerName = customers.stream()
					.filter(c -> c.id().equals(apt.customerId()))
					.map(CustomerRequests.Response::fullName)
					.findFirst()
					.orElse("Không rõ");

			String serviceName = services.stream()
					.filter(s -> s.id().equals(apt.serviceId()))
					.map(ServiceRequests.Response::name)
					.findFirst()
					.orElse("Không rõ");

			tableModel.addRow(new Object[] {
					apt.id(),
					customerName,
					serviceName,
					apt.appointmentTime().format(DATE_FORMATTER),
					apt.status(),
					apt.note() != null ? apt.note() : ""
			});
		}

		// Clear selection
		selectedAppointmentId = null;
		table.clearSelection();
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnPay.setEnabled(false);
		applyAppointmentFilters();
	}

	private void applyAppointmentFilters() {
		if (tableSorter == null) {
			return;
		}

		String customer = normalizeFilterText(txtCustomerSearch);
		String service = selectedComboFilter(cbServiceSearch, 0);
		LocalDateTime dateTime = dateTimeFilterActive ? getSelectedSearchDateTime() : null;
		String status = cbStatusSearch != null && cbStatusSearch.getSelectedIndex() > 0
				? cbStatusSearch.getSelectedItem().toString().toLowerCase(Locale.ROOT)
				: "";

		tableSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
			@Override
			public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
				return contains(entry, 1, customer)
						&& equalsText(entry, 2, service)
						&& equalsDateTime(entry, 3, dateTime)
						&& contains(entry, 4, status);
			}
		});

		selectedAppointmentId = null;
		table.clearSelection();
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnPay.setEnabled(false);

		if (appointments != null && lblStatus != null) {
			int visibleCount = tableSorter.getViewRowCount();
			setStatus("S\u1eb5n s\u00e0ng - " + visibleCount + "/" + appointments.size() + " l\u1ecbch h\u1eb9n");
		}
	}

	private String normalizeFilterText(JTextField field) {
		if (field == null || field.getText() == null) {
			return "";
		}
		return field.getText().trim().toLowerCase(Locale.ROOT);
	}

	private String selectedComboFilter(JComboBox<String> comboBox, int allIndex) {
		if (comboBox == null || comboBox.getSelectedIndex() == allIndex || comboBox.getSelectedItem() == null) {
			return "";
		}
		return comboBox.getSelectedItem().toString().trim().toLowerCase(Locale.ROOT);
	}

	private boolean contains(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry,
			int column, String keyword) {
		if (keyword.isEmpty()) {
			return true;
		}
		Object value = entry.getValue(column);
		return value != null && value.toString().toLowerCase(Locale.ROOT).contains(keyword);
	}

	private boolean equalsText(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry,
			int column, String keyword) {
		if (keyword.isEmpty()) {
			return true;
		}
		Object value = entry.getValue(column);
		return value != null && value.toString().trim().toLowerCase(Locale.ROOT).equals(keyword);
	}

	private boolean equalsDateTime(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry,
			int column, LocalDateTime dateTime) {
		if (dateTime == null) {
			return true;
		}
		Object value = entry.getValue(column);
		if (value == null) {
			return false;
		}
		return LocalDateTime.parse(value.toString(), DATE_FORMATTER).equals(dateTime);
	}

	private LocalDateTime getSelectedSearchDateTime() {
		Date selectedDate = (Date) spDateTimeSearch.getValue();
		return LocalDateTime.ofInstant(selectedDate.toInstant(), ZoneId.systemDefault())
				.withSecond(0)
				.withNano(0);
	}

	private void populateServiceFilter() {
		if (cbServiceSearch == null) {
			return;
		}
		cbServiceSearch.removeAllItems();
		cbServiceSearch.addItem("T\u1ea5t c\u1ea3 d\u1ecbch v\u1ee5");
		if (services != null) {
			services.stream()
					.map(ServiceRequests.Response::name)
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.forEach(cbServiceSearch::addItem);
		}
		cbServiceSearch.setSelectedIndex(0);
	}

	private List<AppointmentRequests.Response> sortByAppointmentTime(List<AppointmentRequests.Response> source) {
		List<AppointmentRequests.Response> sorted = new ArrayList<>(source);
		sorted.sort(Comparator
				.comparing(AppointmentRequests.Response::appointmentTime)
				.thenComparing(AppointmentRequests.Response::id));
		return sorted;
	}

	// ========== BUTTON CLICK HANDLERS ==========

	/**
	 * Xử lý button Add
	 */
	private void onAddButtonClicked() {
		AppointmentDialog dialog = new AppointmentDialog(
				SwingUtilities.getWindowAncestor(this),
				customers, services);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			addAppointment(dialog.getAppointmentCreateRequest());
		}
	}

	/**
	 * Xử lý button Edit
	 */
	private void onEditButtonClicked() {
		if (selectedAppointmentId == null) {
			JOptionPane.showMessageDialog(this,
					"Vui lòng chọn một lịch hẹn để sửa",
					"Không có lựa chọn", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Find the appointment
		AppointmentRequests.Response selectedApt = appointments.stream()
				.filter(a -> a.id().equals(selectedAppointmentId))
				.findFirst()
				.orElse(null);

		if (selectedApt == null) {
			showError("Lịch hẹn không tìm thấy");
			return;
		}

		AppointmentDialog dialog = new AppointmentDialog(
				SwingUtilities.getWindowAncestor(this),
				customers, services, selectedApt);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			updateAppointment(selectedAppointmentId, dialog.getAppointmentUpdateRequest());
		}
	}

	/**
	 * Xử lý button Delete
	 */
	private void onDeleteButtonClicked() {
		if (selectedAppointmentId == null) {
			JOptionPane.showMessageDialog(this,
					"Vui lòng chọn một lịch hẹn để xóa",
					"Không có lựa chọn", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Bạn chắc chắn muốn xóa lịch hẹn này?",
				"Xác nhận xóa", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			deleteAppointment(selectedAppointmentId);
		}
	}

	private void onPayButtonClicked() {
		AppointmentRequests.Response selectedApt = getSelectedAppointment();
		if (selectedApt == null) {
			JOptionPane.showMessageDialog(this,
					"Vui lòng chọn một lịch hẹn để thanh toán",
					"Không có lựa chọn", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		ServiceRequests.Response service = findService(selectedApt.serviceId());
		String customerName = findCustomerName(selectedApt.customerId());
		String serviceName = service != null ? service.name() : "Không rõ";
		BigDecimal amount = service != null ? service.price() : BigDecimal.ZERO;

		PaymentSimulationDialog dialog = new PaymentSimulationDialog(
				SwingUtilities.getWindowAncestor(this),
				selectedApt.id(),
				customerName,
				serviceName,
				amount,
				"QR / chuyển khoản");
		dialog.setVisible(true);

		if (dialog.isPaid()) {
			createSimulatedPayment(selectedApt.id(), amount, PaymentMethod.bank_transfer);
		}
	}

	// ========== CRUD OPERATIONS (Non-blocking with SwingWorker) ==========

	/**
	 * Thêm lịch hẹn mới (Non-blocking)
	 */
	private void addAppointment(AppointmentRequests.Create createReq) {
		setStatus("Đang tạo lịch hẹn...");
		disableButtons();

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
								"Lịch hẹn được tạo thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi tạo lịch hẹn", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};

		worker.execute();
	}

	/**
	 * Cập nhật lịch hẹn (Non-blocking)
	 */
	private void updateAppointment(Integer appointmentId, AppointmentRequests.Update updateReq) {
		setStatus("Đang cập nhật lịch hẹn...");
		disableButtons();

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
								"Lịch hẹn được cập nhật thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi cập nhật lịch hẹn", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};

		worker.execute();
	}

	/**
	 * Xóa lịch hẹn (Non-blocking)
	 */
	private void deleteAppointment(Integer appointmentId) {
		setStatus("Đang xóa lịch hẹn...");
		disableButtons();

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
								"Lịch hẹn được xóa thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi xóa lịch hẹn", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};

		worker.execute();
	}

	private void createSimulatedPayment(Integer appointmentId, BigDecimal amount, PaymentMethod paymentMethod) {
		setStatus("Đang ghi nhận thanh toán...");
		disableButtons();

		PaymentRequests.Create request = new PaymentRequests.Create(
				appointmentId,
				amount,
				paymentMethod,
				PaymentStatus.paid,
				LocalDateTime.now());

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				ApiClient.createPayment(request);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(AppointmentPanel.this,
							"Thanh toán mô phỏng thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadAppointments();
				} catch (Exception e) {
					handleException("Lỗi ghi nhận thanh toán", e);
					enableButtons();
					setStatus("Lỗi");
				}
			}
		};

		worker.execute();
	}

	// ========== HELPER METHODS ==========

	private AppointmentRequests.Response getSelectedAppointment() {
		if (selectedAppointmentId == null || appointments == null) {
			return null;
		}
		return appointments.stream()
				.filter(a -> a.id().equals(selectedAppointmentId))
				.findFirst()
				.orElse(null);
	}

	private ServiceRequests.Response findService(Integer serviceId) {
		if (services == null) {
			return null;
		}
		return services.stream()
				.filter(s -> s.id().equals(serviceId))
				.findFirst()
				.orElse(null);
	}

	private String findCustomerName(Integer customerId) {
		if (customers == null) {
			return "Không rõ";
		}
		return customers.stream()
				.filter(c -> c.id().equals(customerId))
				.map(CustomerRequests.Response::fullName)
				.findFirst()
				.orElse("Không rõ");
	}

	/**
	 * Xử lý exception và hiển thị thông báo lỗi
	 */
	private void handleException(String title, Exception e) {
		String message = e.getMessage();
		if (message == null || message.isEmpty()) {
			message = e.getClass().getSimpleName();
		}

		JOptionPane.showMessageDialog(this,
				title + ": " + message,
				"Lỗi", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Hiển thị thông báo lỗi
	 */
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Cập nhật status label
	 */
	private void setStatus(String status) {
		lblStatus.setText(status);
	}

	/**
	 * Disable tất cả buttons
	 */
	private void disableButtons() {
		btnAdd.setEnabled(false);
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnPay.setEnabled(false);
		btnRefresh.setEnabled(false);
	}

	/**
	 * Enable tất cả buttons
	 */
	private void enableButtons() {
		btnAdd.setEnabled(true);
		btnRefresh.setEnabled(true);
		// Edit và Delete chỉ enable khi có selection
		if (selectedAppointmentId != null) {
			btnEdit.setEnabled(true);
			btnDelete.setEnabled(true);
			btnPay.setEnabled(true);
		}
	}

	/**
	 * Tạo button với listener
	 */
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
}
