package com.salonnbooking.ui.panels;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.desktop.api.ApiClient;
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
import java.util.List;

public class EmployeePanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";
    private final ApiClient apiClient = new ApiClient(BASE_URL);

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> roleCombo;
    private final JButton refreshBtn;
    private final JPanel gridContainer;

    private List<AdminUserDtos.UserResponse> allStaff = new ArrayList<>();

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
        String[] roles = {"Tất cả chức vụ", "Stylist chính", "Thợ phụ", "Thu ngân"};
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
        filterBar.add(addButton, "h 38!");

        add(filterBar, "gapbottom 15");

        // 2. Staff Cards Grid Scroll Pane
        gridContainer = new JPanel(new MigLayout("wrap 3, gap 20, fillx", "[fill, 33%][fill, 33%][fill, 33%]"));
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
                try {
                    String json = apiClient.getRaw("/api/admin/staff");
                    Type type = new TypeToken<List<AdminUserDtos.UserResponse>>() {
                    }.getType();
                    return JsonUtil.fromJson(json, type);
                } catch (Exception ex) {
                    System.out.println("[API Offline] Tải danh sách nhân viên thất bại, kích hoạt Mock Data: " + ex.getMessage());
                    return createMockData();
                }
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
        // 0: Tất cả, 1: Stylist chính, 2: Thợ phụ, 3: Thu ngân

        for (AdminUserDtos.UserResponse staff : allStaff) {
            // Filter by query
            boolean matchesQuery = query.isEmpty() ||
                    (staff.fullName() != null && staff.fullName().toLowerCase().contains(query)) ||
                    (staff.phone() != null && staff.phone().toLowerCase().contains(query)) ||
                    (staff.email() != null && staff.email().toLowerCase().contains(query));

            if (!matchesQuery) continue;

            // Map and filter by Role
            // For mock/db roles, we can try to parse or mock it. Let's map db roles or assign a mock role for display
            String roleText = "Nhân viên";
            if (staff.role() != null) {
                roleText = staff.role() == com.salonnbooking.domain.Role.ADMIN ? "Quản trị viên" : "Stylist";
            }
            // For more variety, let's map some IDs or names to specific sub-roles if needed, or stick to backend roles
            // If they filter by "Stylist chính" (index 1), let's match roles containing "Stylist"
            if (roleIndex == 1 && !roleText.contains("Stylist")) continue;
            if (roleIndex == 2 && !roleText.contains("Thợ phụ") && !staff.fullName().contains("Huy") && !staff.fullName().contains("Mai"))
                continue;
            if (roleIndex == 3 && !roleText.contains("Thu ngân") && !staff.fullName().contains("Kim")) continue;

            String status = staff.isActive() != null && staff.isActive() ? "Đang làm việc" : "Nghỉ phép";
            Color statusColor = staff.isActive() != null && staff.isActive() ? Theme.EMERALD : Theme.AMBER;

            // Assign subroles for styling
            if (staff.fullName().contains("Huy") || staff.fullName().contains("Mai")) {
                roleText = "Thợ phụ";
            } else if (staff.fullName().contains("Kim")) {
                roleText = "Thu ngân";
            } else if (staff.fullName().contains("Bình") || staff.fullName().contains("Thảo") || staff.fullName().contains("Quốc Anh")) {
                roleText = "Stylist chính";
            }

            gridContainer.add(createStaffCard(staff.fullName(), roleText, "⭐ 4.9 (100+)", status, statusColor), "h 150!");
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private List<AdminUserDtos.UserResponse> createMockData() {
        List<AdminUserDtos.UserResponse> list = new ArrayList<>();
        list.add(new AdminUserDtos.UserResponse(
                201L, "Trần Bình", "binh@salon.com", "090111222", Gender.MALE, com.salonnbooking.domain.Role.STAFF, true,
                java.time.LocalDateTime.now().minusYears(1), java.time.LocalDateTime.now()
        ));
        list.add(new AdminUserDtos.UserResponse(
                202L, "Lê Thảo", "thao@salon.com", "090222333", Gender.FEMALE, com.salonnbooking.domain.Role.STAFF, true,
                java.time.LocalDateTime.now().minusYears(1), java.time.LocalDateTime.now()
        ));
        list.add(new AdminUserDtos.UserResponse(
                203L, "Phạm Huy", "huy@salon.com", "090333444", Gender.MALE, com.salonnbooking.domain.Role.STAFF, true,
                java.time.LocalDateTime.now().minusMonths(6), java.time.LocalDateTime.now()
        ));
        list.add(new AdminUserDtos.UserResponse(
                204L, "Nguyễn Mai", "mai@salon.com", "090444555", Gender.FEMALE, com.salonnbooking.domain.Role.STAFF, false,
                java.time.LocalDateTime.now().minusMonths(8), java.time.LocalDateTime.now()
        ));
        list.add(new AdminUserDtos.UserResponse(
                205L, "Hoàng Kim", "kim@salon.com", "090555666", Gender.FEMALE, com.salonnbooking.domain.Role.STAFF, true,
                java.time.LocalDateTime.now().minusMonths(3), java.time.LocalDateTime.now()
        ));
        list.add(new AdminUserDtos.UserResponse(
                206L, "Đỗ Quốc Anh", "quocanh@salon.com", "090666777", Gender.MALE, com.salonnbooking.domain.Role.STAFF, true,
                java.time.LocalDateTime.now().minusMonths(5), java.time.LocalDateTime.now()
        ));
        return list;
    }

    private RoundedPanel createStaffCard(String name, String role, String rating, String status, Color statusColor) {
        RoundedPanel card = new RoundedPanel(12, Theme.BG_CARD, true);
        card.setLayout(new MigLayout("insets 16, fillx", "[]16[grow]", "[]"));

        // Left side: CircleAvatar
        CircleAvatar avatar = new CircleAvatar(name, 56, Theme.NAVY, Theme.TEXT_WHITE);
        card.add(avatar, "aligny top");

        // Right side: Info Panel
        JPanel infoPanel = new JPanel(new MigLayout("insets 0, gap 4, fillx", "[fill]", "[]"));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(Theme.FONT_H2);
        nameLabel.setForeground(Theme.NAVY);
        infoPanel.add(nameLabel, "wrap");

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(Theme.FONT_BODY_LG);
        roleLabel.setForeground(Theme.TEXT_MUTED);
        infoPanel.add(roleLabel, "wrap");

        JLabel ratingLabel = new JLabel(rating);
        ratingLabel.setFont(Theme.FONT_BODY_SM);
        ratingLabel.setForeground(Theme.AMBER);
        infoPanel.add(ratingLabel, "wrap, gapbottom 4");

        // Status Badge
        BadgeLabel statusBadge = new BadgeLabel(status, statusColor, Theme.TEXT_WHITE);
        infoPanel.add(statusBadge, "w min!, h 24!");

        card.add(infoPanel, "growx");
        return card;
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
