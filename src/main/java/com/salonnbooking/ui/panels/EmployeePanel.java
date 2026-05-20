package com.salonnbooking.ui.panels;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.api.dto.AdminStaffServiceDtos;
import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.api.ApiException;
import com.salonnbooking.desktop.util.JsonUtil;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.ui.components.CircleAvatar;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmployeePanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";
    private final ApiClient apiClient = new ApiClient(BASE_URL);

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> roleCombo;
    private final JButton refreshBtn;
    private final JPanel gridContainer;

    private List<AdminUserDtos.UserResponse> allStaff = new ArrayList<>();

    private record GenderOption(Gender value, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public EmployeePanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][fill, grow]"));

        // 1. Top Filter Bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm nhân viên...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

        // Role Filter
        String[] roles = {"Tất cả chức vụ", "Nhân viên", "Quản trị viên"};
        roleCombo = new JComboBox<>(roles);
        filterBar.add(roleCombo, "gapright 20");

        // Refresh Button
        refreshBtn = new JButton("Làm mới");
        refreshBtn.setFont(Theme.FONT_BODY_LG);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBar.add(refreshBtn, "h 38!, gapright 10");

        // Add Employee Button
        JButton addButton = new JButton("+ Thêm nhân viên");
        addButton.setFont(Theme.FONT_H3);
        addButton.setForeground(Theme.TEXT_WHITE);
        addButton.setBackground(Theme.EMERALD);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showStaffDialog(null));
        filterBar.add(addButton, "h 38!");

        add(filterBar, "gapbottom 15");

        // 2. Staff Cards List Scroll Pane
        gridContainer = new JPanel(new MigLayout(
                "wrap 1, gapy 14, fillx, insets 0",
                "[fill]",
                ""));
        gridContainer.setOpaque(false);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, "grow");

        // Listeners for filter inputs
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                renderStaffGrid();
            }
        });
        roleCombo.addActionListener(e -> renderStaffGrid());
        refreshBtn.addActionListener(e -> loadStaff());

        // Initial Load
        loadStaff();
    }

    private void setLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        refreshBtn.setEnabled(!loading);
        searchField.setEnabled(!loading);
        roleCombo.setEnabled(!loading);
    }

    private void loadStaff() {
        setLoading(true);
        SwingWorker<List<AdminUserDtos.UserResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AdminUserDtos.UserResponse> doInBackground() throws Exception {
                String json = apiClient.getRaw("/api/admin/staff");
                Type type = new TypeToken<List<AdminUserDtos.UserResponse>>() {
                }.getType();
                return JsonUtil.fromJson(json, type);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    allStaff = get();
                    renderStaffGrid();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeePanel.this,
                            "Lỗi hiển thị danh sách nhân viên: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void renderStaffGrid() {
        gridContainer.removeAll();

        String query = searchField.getText().trim().toLowerCase();
        int roleIndex = roleCombo.getSelectedIndex();
        int rendered = 0;

        for (AdminUserDtos.UserResponse staff : allStaff) {
            boolean matchesQuery = query.isEmpty() ||
                    (staff.fullName() != null && staff.fullName().toLowerCase().contains(query)) ||
                    (staff.phone() != null && staff.phone().toLowerCase().contains(query)) ||
                    (staff.email() != null && staff.email().toLowerCase().contains(query));

            if (!matchesQuery) continue;

            String roleText = "Nhân viên";
            if (staff.role() != null) {
                roleText = staff.role() == com.salonnbooking.domain.Role.ADMIN ? "Quản trị viên" : "Stylist";
            }
            if (roleIndex == 1 && staff.role() != com.salonnbooking.domain.Role.STAFF) continue;
            if (roleIndex == 2 && staff.role() != com.salonnbooking.domain.Role.ADMIN) continue;

            String status = staff.isActive() != null && staff.isActive() ? "Đang làm việc" : "Nghỉ phép";
            Color statusColor = staff.isActive() != null && staff.isActive() ? Theme.EMERALD : Theme.AMBER;

            gridContainer.add(createStaffCard(staff, roleText, status, statusColor), "growx, h 128!");
            rendered++;
        }

        if (rendered == 0) {
            RoundedPanel emptyCard = new RoundedPanel(12, Theme.BG_CARD, true);
            emptyCard.setLayout(new MigLayout("insets 24, fillx", "[center]", "[center]"));
            JLabel empty = new JLabel("Không có nhân viên phù hợp");
            empty.setFont(Theme.FONT_H3);
            empty.setForeground(Theme.TEXT_MUTED);
            emptyCard.add(empty);
            gridContainer.add(emptyCard, "growx, h 96!");
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private RoundedPanel createStaffCard(AdminUserDtos.UserResponse staff, String role, String status, Color statusColor) {
        RoundedPanel card = new RoundedPanel(12, Theme.BG_CARD, true);
        card.setLayout(new MigLayout(
                "insets 16, fillx",
                "[]16[grow]push[right]",
                "[]"));

        String name = staff.fullName() != null ? staff.fullName() : "Nhân viên";
        CircleAvatar avatar = new CircleAvatar(name, 56, Theme.NAVY, Theme.TEXT_WHITE);
        card.add(avatar, "aligny top");

        JPanel infoPanel = new JPanel(new MigLayout("wrap 1, insets 0, gapy 4, fillx", "[fill]", "[]"));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(Theme.FONT_H2);
        nameLabel.setForeground(Theme.NAVY);
        infoPanel.add(nameLabel);

        JLabel roleLabel = new JLabel(role + formatContact(staff));
        roleLabel.setFont(Theme.FONT_BODY_LG);
        roleLabel.setForeground(Theme.TEXT_MUTED);
        infoPanel.add(roleLabel);

        BadgeLabel statusBadge = new BadgeLabel(status, statusColor, Theme.TEXT_WHITE);
        infoPanel.add(statusBadge, "w min!, h 24!");

        card.add(infoPanel, "growx");

        JPanel actions = new JPanel(new MigLayout("wrap 1, insets 0, gapy 6", "[fill]"));
        actions.setOpaque(false);
        JLabel idLabel = new JLabel("#" + staff.id(), SwingConstants.RIGHT);
        idLabel.setFont(Theme.FONT_H3);
        idLabel.setForeground(Theme.TEXT_MUTED);
        actions.add(idLabel, "growx");

        JPanel buttons = new JPanel(new MigLayout("insets 0, gap 6", "[][][]"));
        buttons.setOpaque(false);
        JButton editBtn = createActionButton("Sửa", Theme.BLUE);
        editBtn.addActionListener(e -> showStaffDialog(staff));
        JButton toggleBtn = createActionButton(Boolean.TRUE.equals(staff.isActive()) ? "Khóa" : "Mở", Theme.AMBER);
        toggleBtn.addActionListener(e -> toggleStaff(staff));
        JButton deleteBtn = createActionButton("Xóa", Theme.CRIMSON);
        deleteBtn.addActionListener(e -> deleteStaff(staff));
        buttons.add(editBtn, "h 30!");
        buttons.add(toggleBtn, "h 30!");
        buttons.add(deleteBtn, "h 30!");
        actions.add(buttons);
        card.add(actions, "aligny top");
        return card;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(Theme.FONT_BODY_SM.deriveFont(Font.BOLD));
        button.setForeground(Theme.TEXT_WHITE);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showStaffDialog(AdminUserDtos.UserResponse staff) {
        boolean editing = staff != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), editing ? "Cập nhật nhân viên" : "Thêm nhân viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new MigLayout("wrap 2, insets 22, gapx 12, gapy 10", "[][grow, fill]", ""));
        form.setBackground(Theme.BG_CARD);

        JTextField nameField = new JTextField(editing && staff.fullName() != null ? staff.fullName() : "");
        JTextField emailField = new JTextField(editing && staff.email() != null ? staff.email() : "");
        JTextField phoneField = new JTextField(editing && staff.phone() != null ? staff.phone() : "");
        JPasswordField passwordField = new JPasswordField();
        JComboBox<GenderOption> genderCombo = new JComboBox<>(new GenderOption[] {
                new GenderOption(Gender.MALE, "Nam"),
                new GenderOption(Gender.FEMALE, "Nữ"),
                new GenderOption(Gender.OTHER, "Khác")
        });
        JCheckBox activeCheck = new JCheckBox("Đang hoạt động");
        activeCheck.setOpaque(false);
        activeCheck.setSelected(!editing || Boolean.TRUE.equals(staff.isActive()));
        if (editing && staff.gender() != null) {
            for (int i = 0; i < genderCombo.getItemCount(); i++) {
                if (genderCombo.getItemAt(i).value() == staff.gender()) {
                    genderCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        List<ServiceDtos.Response> services = loadServicesForForm();
        Set<Long> selectedServiceIds = editing ? loadAssignedServiceIds(staff.id()) : Set.of();
        JPanel serviceChecksPanel = new JPanel(new MigLayout("wrap 2, insets 0, gapx 16, gapy 6", "[grow][grow]", ""));
        serviceChecksPanel.setOpaque(false);
        List<JCheckBox> serviceChecks = new ArrayList<>();
        for (ServiceDtos.Response service : services) {
            JCheckBox serviceCheck = new JCheckBox(service.name());
            serviceCheck.putClientProperty("serviceId", service.id());
            serviceCheck.setOpaque(false);
            serviceCheck.setSelected(selectedServiceIds.contains(service.id()));
            serviceChecks.add(serviceCheck);
            serviceChecksPanel.add(serviceCheck, "growx");
        }
        JScrollPane serviceScroll = new JScrollPane(serviceChecksPanel);
        serviceScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        serviceScroll.getViewport().setOpaque(false);
        serviceScroll.setOpaque(false);

        addFormRow(form, "Họ tên", nameField);
        addFormRow(form, "Email", emailField);
        addFormRow(form, "SĐT", phoneField);
        if (!editing) {
            addFormRow(form, "Mật khẩu", passwordField);
        }
        addFormRow(form, "Giới tính", genderCombo);
        form.add(new JLabel(""));
        form.add(activeCheck);
        JLabel servicesLabel = new JLabel("Dịch vụ phụ trách:");
        servicesLabel.setFont(Theme.FONT_H3);
        servicesLabel.setForeground(Theme.TEXT_PRIMARY);
        form.add(servicesLabel, "aligny top");
        form.add(serviceScroll, "growx, h 130!");

        JPanel footer = new JPanel(new MigLayout("insets 10 22 18 22, fillx", "push[][]"));
        footer.setBackground(Theme.BG_CARD);
        JButton cancelBtn = new JButton("Hủy");
        JButton saveBtn = new JButton(editing ? "Lưu" : "Tạo nhân viên");
        saveBtn.setBackground(Theme.EMERALD);
        saveBtn.setForeground(Theme.TEXT_WHITE);
        saveBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isBlank() || emailField.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Họ tên và email là bắt buộc", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!editing && new String(passwordField.getPassword()).isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu là bắt buộc", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (editing) {
                GenderOption selectedGender = (GenderOption) genderCombo.getSelectedItem();
                AdminUserDtos.UpdateUserRequest request = new AdminUserDtos.UpdateUserRequest(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        selectedGender != null ? selectedGender.value() : null,
                        activeCheck.isSelected());
                runStaffAction("Cập nhật nhân viên", () -> {
                    apiClient.put("/api/admin/staff/" + staff.id(), request, AdminUserDtos.UserResponse.class);
                    syncStaffServices(staff.id(), serviceChecks);
                });
            } else {
                GenderOption selectedGender = (GenderOption) genderCombo.getSelectedItem();
                AdminUserDtos.CreateStaffRequest request = new AdminUserDtos.CreateStaffRequest(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        new String(passwordField.getPassword()),
                        selectedGender != null ? selectedGender.value() : null,
                        activeCheck.isSelected());
                runStaffAction("Tạo nhân viên", () -> {
                    AdminUserDtos.UserResponse created = apiClient.post("/api/admin/staff", request, AdminUserDtos.UserResponse.class);
                    syncStaffServices(created.id(), serviceChecks);
                });
            }
            dialog.dispose();
        });
        footer.add(cancelBtn, "h 36!");
        footer.add(saveBtn, "h 36!");

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private List<ServiceDtos.Response> loadServicesForForm() {
        try {
            String json = apiClient.getRaw("/api/admin/services");
            Type type = new TypeToken<List<ServiceDtos.Response>>() {}.getType();
            return JsonUtil.fromJson(json, type);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách dịch vụ: " + errorMessage(ex),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private Set<Long> loadAssignedServiceIds(Long staffId) {
        try {
            String json = apiClient.getRaw("/api/admin/staff/" + staffId + "/services");
            Type type = new TypeToken<List<AdminStaffServiceDtos.StaffServiceResponse>>() {}.getType();
            List<AdminStaffServiceDtos.StaffServiceResponse> assigned = JsonUtil.fromJson(json, type);
            Set<Long> ids = new HashSet<>();
            for (AdminStaffServiceDtos.StaffServiceResponse item : assigned) {
                if (item.serviceId() != null) {
                    ids.add(item.serviceId());
                }
            }
            return ids;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được dịch vụ nhân viên đang phụ trách: " + errorMessage(ex),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return Set.of();
        }
    }

    private void syncStaffServices(Long staffId, List<JCheckBox> serviceChecks) throws Exception {
        Set<Long> current = loadAssignedServiceIds(staffId);
        Set<Long> desired = new HashSet<>();
        for (JCheckBox check : serviceChecks) {
            Long serviceId = (Long) check.getClientProperty("serviceId");
            if (serviceId != null && check.isSelected()) {
                desired.add(serviceId);
            }
        }

        for (Long serviceId : desired) {
            if (!current.contains(serviceId)) {
                apiClient.post("/api/admin/staff/" + staffId + "/services/" + serviceId, null, AdminStaffServiceDtos.StaffServiceResponse.class);
            }
        }
        for (Long serviceId : current) {
            if (!desired.contains(serviceId)) {
                apiClient.delete("/api/admin/staff/" + staffId + "/services/" + serviceId, Void.class);
            }
        }
    }

    private void addFormRow(JPanel form, String label, JComponent field) {
        JLabel labelView = new JLabel(label + ":");
        labelView.setFont(Theme.FONT_H3);
        labelView.setForeground(Theme.TEXT_PRIMARY);
        form.add(labelView);
        form.add(field, "h 36!, growx");
    }

    private void toggleStaff(AdminUserDtos.UserResponse staff) {
        String action = Boolean.TRUE.equals(staff.isActive()) ? "khóa" : "mở lại";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn muốn " + action + " nhân viên " + staff.fullName() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        runStaffAction("Cập nhật trạng thái", () -> apiClient.patch("/api/admin/staff/" + staff.id() + "/toggle-active", null, AdminUserDtos.UserResponse.class));
    }

    private void deleteStaff(AdminUserDtos.UserResponse staff) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa nhân viên " + staff.fullName() + "?\nNếu nhân viên đã có lịch hẹn, hệ thống sẽ không cho xóa.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        runStaffAction("Xóa nhân viên", () -> apiClient.delete("/api/admin/staff/" + staff.id(), Void.class));
    }

    private void runStaffAction(String title, StaffAction action) {
        setLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                action.run();
                return null;
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    loadStaff();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeePanel.this,
                            title + " thất bại: " + errorMessage(ex),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String errorMessage(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof ApiException apiException && apiException.getResponseBody() != null && !apiException.getResponseBody().isBlank()) {
            return apiException.getResponseBody();
        }
        return cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
    }

    @FunctionalInterface
    private interface StaffAction {
        void run() throws Exception;
    }

    private String formatContact(AdminUserDtos.UserResponse staff) {
        List<String> parts = new ArrayList<>();
        if (staff.email() != null && !staff.email().isBlank()) {
            parts.add(staff.email());
        }
        if (staff.phone() != null && !staff.phone().isBlank()) {
            parts.add(staff.phone());
        }
        return parts.isEmpty() ? "" : " • " + String.join(" • ", parts);
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
            setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}
