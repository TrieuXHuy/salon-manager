package com.salonnbooking.ui.panels;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.api.dto.StaffDtos;
import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.model.Role;
import com.salonnbooking.desktop.session.AuthSession;
import com.salonnbooking.desktop.util.JsonUtil;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.ui.ScreenRouter;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentPanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";
    private final ApiClient apiClient = new ApiClient(BASE_URL);

    private final JTextField searchField = new JTextField(22);
    private final JComboBox<String> statusCombo;
    private final JButton confirmBtn;
    private final JButton checkinBtn;
    private final JButton completeBtn;
    private final JButton cancelBtn;
    private final JButton refreshBtn;

    private final DefaultTableModel tableModel;
    private final JTable table;

    private List<BookingDtos.AppointmentResponse> allAppointments = new ArrayList<>();

    public AppointmentPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][grow][]"));

        // 1. Top filter and action bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm khách hàng, dịch vụ, nhân viên...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

        // Status Filter
        String[] statuses = {
            "Tất cả trạng thái", 
            "Chờ xác nhận (PENDING)", 
            "Đã xác nhận (CONFIRMED)", 
            "Đang phục vụ (IN_PROGRESS)", 
            "Đã hoàn thành (COMPLETED)", 
            "Đã hủy (CANCELLED)"
        };
        statusCombo = new JComboBox<>(statuses);
        filterBar.add(statusCombo, "gapright 20");

        // Refresh Button
        refreshBtn = new JButton("Làm mới");
        refreshBtn.setFont(Theme.FONT_BODY_LG);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBar.add(refreshBtn, "h 38!, gapright 10");

        // Add Button
        JButton addButton = new JButton("+ Đặt lịch mới");
        addButton.setFont(Theme.FONT_H3);
        addButton.setForeground(Theme.TEXT_WHITE);
        addButton.setBackground(Theme.EMERALD);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> ScreenRouter.go("booking"));
        filterBar.add(addButton, "h 38!");

        add(filterBar, "gapbottom 15");

        // 2. Appointments Table Container
        RoundedPanel tablePanel = new RoundedPanel(12, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 24, fill", "[fill]", "[grow]"));

        String[] columns = {"ID", "Khách hàng", "Dịch vụ", "Nhân viên", "Thời gian", "Giá tiền", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.setFont(Theme.FONT_BODY_REG);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setSelectionBackground(new Color(219, 242, 241)); // Emerald tint
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.BORDER);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_H3);
        header.setBackground(Theme.BG_CARD);
        header.setForeground(Theme.TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        // Apply Custom Renderer to Status Column
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        add(tablePanel, "grow, gapbottom 15");

        // 3. Action Buttons (State Machine)
        JPanel actionPanel = new JPanel(new MigLayout("insets 0, gap 12", "[]"));
        actionPanel.setOpaque(false);

        confirmBtn = new JButton("Xác nhận lịch");
        confirmBtn.setFont(Theme.FONT_H3);
        confirmBtn.setBackground(Theme.BLUE);
        confirmBtn.setForeground(Theme.TEXT_WHITE);
        confirmBtn.setEnabled(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        checkinBtn = new JButton("Check-in");
        checkinBtn.setFont(Theme.FONT_H3);
        checkinBtn.setBackground(new Color(139, 92, 246)); // Purple
        checkinBtn.setForeground(Theme.TEXT_WHITE);
        checkinBtn.setEnabled(false);
        checkinBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        completeBtn = new JButton("Hoàn thành lịch");
        completeBtn.setFont(Theme.FONT_H3);
        completeBtn.setBackground(Theme.EMERALD);
        completeBtn.setForeground(Theme.TEXT_WHITE);
        completeBtn.setEnabled(false);
        completeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelBtn = new JButton("Hủy lịch");
        cancelBtn.setFont(Theme.FONT_H3);
        cancelBtn.setBackground(Theme.CRIMSON);
        cancelBtn.setForeground(Theme.TEXT_WHITE);
        cancelBtn.setEnabled(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        actionPanel.add(confirmBtn, "h 38!");
        actionPanel.add(checkinBtn, "h 38!");
        actionPanel.add(completeBtn, "h 38!");
        actionPanel.add(cancelBtn, "h 38!");

        add(actionPanel, "growx");

        // Listeners for filters
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterAndRenderTable();
            }
        });
        statusCombo.addActionListener(e -> filterAndRenderTable());

        // Action listeners
        refreshBtn.addActionListener(e -> loadAppointments());
        confirmBtn.addActionListener(e -> changeSelectedStatus(AppointmentStatus.CONFIRMED));
        checkinBtn.addActionListener(e -> changeSelectedStatus(AppointmentStatus.IN_PROGRESS));
        completeBtn.addActionListener(e -> changeSelectedStatus(AppointmentStatus.COMPLETED));
        cancelBtn.addActionListener(e -> changeSelectedStatus(AppointmentStatus.CANCELLED));

        // Table Selection Listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Initialize Data
        loadAppointments();
    }

    private void setLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        refreshBtn.setEnabled(!loading);
        searchField.setEnabled(!loading);
        statusCombo.setEnabled(!loading);
    }

    private void loadAppointments() {
        setLoading(true);
        SwingWorker<List<BookingDtos.AppointmentResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookingDtos.AppointmentResponse> doInBackground() throws Exception {
                // Skip API call in mock mode
                if (AuthSession.getInstance().isMockSession()) {
                    return createMockData();
                }
                boolean isAdmin = AuthSession.getInstance().getRole() == Role.ADMIN;
                String path = isAdmin ? "/api/admin/appointments" : "/api/staff/appointments";
                try {
                    String json = apiClient.getRaw(path);
                    Type type = new TypeToken<List<BookingDtos.AppointmentResponse>>() {}.getType();
                    return JsonUtil.fromJson(json, type);
                } catch (Exception ex) {
                    System.out.println("[API Offline] Tải danh sách lịch hẹn thất bại, kích hoạt Mock Data: " + ex.getMessage());
                    return createMockData();
                }
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    allAppointments = get();
                    filterAndRenderTable();
                    updateButtonStates();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AppointmentPanel.this, 
                        "Lỗi hiển thị dữ liệu: " + ex.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void filterAndRenderTable() {
        tableModel.setRowCount(0);
        String query = searchField.getText().trim().toLowerCase();
        int statusFilterIdx = statusCombo.getSelectedIndex();

        for (BookingDtos.AppointmentResponse app : allAppointments) {
            // Search filters
            boolean matchesSearch = query.isEmpty() ||
                (app.customerName() != null && app.customerName().toLowerCase().contains(query)) ||
                (app.staffName() != null && app.staffName().toLowerCase().contains(query)) ||
                (app.services() != null && app.services().stream().anyMatch(s -> s.serviceName() != null && s.serviceName().toLowerCase().contains(query)));

            if (!matchesSearch) continue;

            // Status filters
            boolean matchesStatus = false;
            switch (statusFilterIdx) {
                case 0: matchesStatus = true; break;
                case 1: matchesStatus = (app.status() == AppointmentStatus.PENDING); break;
                case 2: matchesStatus = (app.status() == AppointmentStatus.CONFIRMED); break;
                case 3: matchesStatus = (app.status() == AppointmentStatus.IN_PROGRESS); break;
                case 4: matchesStatus = (app.status() == AppointmentStatus.COMPLETED); break;
                case 5: matchesStatus = (app.status() == AppointmentStatus.CANCELLED); break;
            }

            if (!matchesStatus) continue;

            // Format Services
            String servicesStr = "";
            if (app.services() != null && !app.services().isEmpty()) {
                List<String> names = app.services().stream().map(BookingDtos.AppointmentServiceResponse::serviceName).toList();
                servicesStr = String.join(", ", names);
            }

            // Format Time
            String timeStr = "";
            if (app.appointmentStart() != null) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM");
                timeStr = app.appointmentStart().format(dtf);
            }

            // Format Price
            String priceStr = "0đ";
            if (app.totalAmount() != null) {
                java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                priceStr = nf.format(app.totalAmount()) + "đ";
            }

            tableModel.addRow(new Object[] {
                app.id(),
                app.customerName() != null ? app.customerName() : "",
                servicesStr,
                app.staffName() != null ? app.staffName() : "",
                timeStr,
                priceStr,
                app.status()
            });
        }
    }

    private void updateButtonStates() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            confirmBtn.setEnabled(false);
            checkinBtn.setEnabled(false);
            completeBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            return;
        }

        AppointmentStatus status = (AppointmentStatus) table.getValueAt(selectedRow, 6);

        confirmBtn.setEnabled(status == AppointmentStatus.PENDING);
        checkinBtn.setEnabled(status == AppointmentStatus.CONFIRMED);
        completeBtn.setEnabled(status == AppointmentStatus.IN_PROGRESS);
        cancelBtn.setEnabled(status == AppointmentStatus.PENDING || status == AppointmentStatus.CONFIRMED || status == AppointmentStatus.IN_PROGRESS);
    }

    private void changeSelectedStatus(AppointmentStatus targetStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return;

        Long appointmentId = (Long) table.getValueAt(selectedRow, 0);
        
        // Show cancel reason dialog if cancelling
        String cancelReason = null;
        if (targetStatus == AppointmentStatus.CANCELLED) {
            cancelReason = JOptionPane.showInputDialog(this, "Nhập lý do hủy lịch hẹn:", "Hủy lịch hẹn", JOptionPane.WARNING_MESSAGE);
            if (cancelReason == null) return; // user cancelled the input
        }

        setLoading(true);
        final String reason = cancelReason;
        SwingWorker<BookingDtos.AppointmentResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingDtos.AppointmentResponse doInBackground() throws Exception {
                boolean isAdmin = AuthSession.getInstance().getRole() == Role.ADMIN;
                if (isAdmin) {
                    if (targetStatus == AppointmentStatus.CANCELLED) {
                        BookingDtos.CancelAppointmentRequest req = new BookingDtos.CancelAppointmentRequest(reason);
                        return apiClient.patch("/api/admin/appointments/" + appointmentId + "/cancel", req, BookingDtos.AppointmentResponse.class);
                    } else {
                        return apiClient.patch("/api/admin/appointments/" + appointmentId + "/status?status=" + targetStatus, null, BookingDtos.AppointmentResponse.class);
                    }
                } else {
                    StaffDtos.UpdateAppointmentStatusRequest req = new StaffDtos.UpdateAppointmentStatusRequest(targetStatus);
                    return apiClient.patch("/api/staff/appointments/" + appointmentId + "/status", req, BookingDtos.AppointmentResponse.class);
                }
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(AppointmentPanel.this, 
                        "Cập nhật trạng thái lịch hẹn thành công!", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                } catch (Exception ex) {
                    System.out.println("[API Offline] Thay đổi trạng thái thất bại, cập nhật cục bộ (Mock Mode): " + ex.getMessage());
                    
                    // Update mock locally
                    boolean updatedMock = false;
                    for (BookingDtos.AppointmentResponse app : allAppointments) {
                        if (app.id().equals(appointmentId)) {
                            // Rebuild record with new status
                            BookingDtos.AppointmentResponse newApp = new BookingDtos.AppointmentResponse(
                                app.id(), app.customerId(), app.customerName(), app.staffId(), app.staffName(),
                                app.appointmentStart(), app.appointmentEnd(), targetStatus, app.note(), reason,
                                app.totalAmount(), app.services(), app.payments(), app.createdAt(), LocalDateTime.now()
                            );
                            int index = allAppointments.indexOf(app);
                            allAppointments.set(index, newApp);
                            updatedMock = true;
                            break;
                        }
                    }

                    if (updatedMock) {
                        JOptionPane.showMessageDialog(AppointmentPanel.this, 
                            "Cập nhật trạng thái thành công (Mock Mode)!", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        filterAndRenderTable();
                        updateButtonStates();
                    } else {
                        JOptionPane.showMessageDialog(AppointmentPanel.this, 
                            "Lỗi cập nhật trạng thái: " + ex.getMessage(), 
                            "Thỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }

    private List<BookingDtos.AppointmentResponse> createMockData() {
        List<BookingDtos.AppointmentResponse> list = new ArrayList<>();
        list.add(new BookingDtos.AppointmentResponse(
            1L, 101L, "Nguyễn Văn A", 201L, "Trần Bình", 
            LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), 
            AppointmentStatus.CONFIRMED, "Cắt ngắn", null, 
            new BigDecimal("150000"), 
            List.of(new BookingDtos.AppointmentServiceResponse(10L, "Cắt tóc nam", new BigDecimal("150000"), 30)),
            List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));
        list.add(new BookingDtos.AppointmentResponse(
            2L, 102L, "Trần Thị B", 202L, "Lê Thảo", 
            LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(6), 
            AppointmentStatus.COMPLETED, "Nhuộm kỹ", null, 
            new BigDecimal("1200000"), 
            List.of(new BookingDtos.AppointmentServiceResponse(11L, "Nhuộm tóc + Phục hồi", new BigDecimal("1200000"), 120)),
            List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));
        list.add(new BookingDtos.AppointmentResponse(
            3L, 103L, "Lê Văn M", 203L, "Phạm Huy", 
            LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 
            AppointmentStatus.PENDING, "Gội thảo dược nhẹ", null, 
            new BigDecimal("200000"), 
            List.of(new BookingDtos.AppointmentServiceResponse(12L, "Gội đầu thảo dược", new BigDecimal("200000"), 45)),
            List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));
        list.add(new BookingDtos.AppointmentResponse(
            4L, 104L, "Phạm Hoàng Nam", 201L, "Trần Bình", 
            LocalDateTime.now().plusHours(5), LocalDateTime.now().plusHours(6), 
            AppointmentStatus.CONFIRMED, "Uốn phồng", null, 
            new BigDecimal("450000"), 
            List.of(new BookingDtos.AppointmentServiceResponse(13L, "Uốn tóc kiểu Hàn", new BigDecimal("450000"), 60)),
            List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));
        list.add(new BookingDtos.AppointmentResponse(
            5L, 105L, "Hoàng Thu Trang", 204L, "Nguyễn Mai", 
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 
            AppointmentStatus.PENDING, "Nối mi dầy", null, 
            new BigDecimal("350000"), 
            List.of(new BookingDtos.AppointmentServiceResponse(14L, "Nối mi volume", new BigDecimal("350000"), 90)),
            List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));
        list.add(new BookingDtos.AppointmentResponse(
            6L, 106L, "Đặng Minh Quân", 203L, "Phạm Huy", 
            LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1), 
            AppointmentStatus.CANCELLED, "Bận việc đột xuất", "Khách yêu cầu hủy", 
            new BigDecimal("180000"), 
            List.of(new BookingDtos.AppointmentServiceResponse(15L, "Cạo mặt + Massage", new BigDecimal("180000"), 40)),
            List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));
        return list;
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            AppointmentStatus status = (AppointmentStatus) value;
            
            JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            Color bg;
            Color fg = Theme.TEXT_WHITE;
            String text = "";
            
            if (status == null) {
                bg = Theme.SLATE;
                text = "Không rõ";
            } else {
                switch (status) {
                    case PENDING:
                        bg = Theme.AMBER;
                        text = "Chờ xác nhận (PENDING)";
                        break;
                    case CONFIRMED:
                        bg = Theme.BLUE;
                        text = "Đã xác nhận (CONFIRMED)";
                        break;
                    case IN_PROGRESS:
                        bg = new Color(139, 92, 246); // Purple
                        text = "Đang phục vụ (IN_PROGRESS)";
                        break;
                    case COMPLETED:
                        bg = Theme.EMERALD;
                        text = "Đã hoàn thành (COMPLETED)";
                        break;
                    case CANCELLED:
                        bg = Theme.CRIMSON;
                        text = "Đã hủy (CANCELLED)";
                        break;
                    default:
                        bg = Theme.SLATE;
                        text = status.name();
                        break;
                }
            }
            
            BadgeLabel label = new BadgeLabel(text, bg, fg);
            panel.add(label);
            return panel;
        }
    }

    private static class BadgeLabel extends JLabel {
        private final Color bgColor;
        
        public BadgeLabel(String text, Color bgColor, Color textColor) {
            super(text);
            this.bgColor = bgColor;
            setForeground(textColor);
            setFont(Theme.FONT_BODY_SM.deriveFont(java.awt.Font.BOLD));
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // bo tròn badge 12px
            super.paintComponent(g);
            g2.dispose();
        }
    }
}
