package com.salonnbooking.ui.panels;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.util.JsonUtil;
import com.salonnbooking.domain.Gender;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";
    private final ApiClient apiClient = new ApiClient(BASE_URL);

    private final JTextField searchField = new JTextField(24);
    private final JButton refreshBtn;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private List<AdminUserDtos.UserResponse> allCustomers = new ArrayList<>();

    public CustomerPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][fill, grow]"));

        // 1. Filter and Action Bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên, số điện thoại, email...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

        // Refresh Button
        refreshBtn = new JButton("Làm mới");
        refreshBtn.setFont(Theme.FONT_BODY_LG);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBar.add(refreshBtn, "h 38!, gapright 10");

        // Add Customer Button
        JButton addButton = new JButton("+ Thêm khách hàng");
        addButton.setFont(Theme.FONT_H3);
        addButton.setForeground(Theme.TEXT_WHITE);
        addButton.setBackground(Theme.EMERALD);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBar.add(addButton, "h 38!");

        add(filterBar, "gapbottom 15");

        // 2. Customers Table
        RoundedPanel tablePanel = new RoundedPanel(12, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 24, fill", "[fill]", "[grow]"));

        String[] columns = {"ID", "Họ và tên", "Email", "Số điện thoại", "Giới tính", "Trạng thái", "Ngày tham gia"};
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
        table.setSelectionBackground(new Color(219, 242, 241)); // Emerald selection
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

        // Custom Cell Renderers
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        add(tablePanel, "grow");

        // Key adapter for instant local filtering
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterAndRenderTable();
            }
        });

        refreshBtn.addActionListener(e -> loadCustomers());

        // Initial load
        loadCustomers();
    }

    private void setLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        refreshBtn.setEnabled(!loading);
        searchField.setEnabled(!loading);
    }

    private void loadCustomers() {
        setLoading(true);
        SwingWorker<List<AdminUserDtos.UserResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AdminUserDtos.UserResponse> doInBackground() throws Exception {
                String json = apiClient.getRaw("/api/admin/customers");
                Type type = new TypeToken<List<AdminUserDtos.UserResponse>>() {}.getType();
                return JsonUtil.fromJson(json, type);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    allCustomers = get();
                    filterAndRenderTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CustomerPanel.this, 
                        "Lỗi hiển thị danh sách khách hàng: " + ex.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void filterAndRenderTable() {
        tableModel.setRowCount(0);
        String query = searchField.getText().trim().toLowerCase();

        for (AdminUserDtos.UserResponse customer : allCustomers) {
            boolean matches = query.isEmpty() ||
                (customer.fullName() != null && customer.fullName().toLowerCase().contains(query)) ||
                (customer.phone() != null && customer.phone().toLowerCase().contains(query)) ||
                (customer.email() != null && customer.email().toLowerCase().contains(query));

            if (!matches) continue;

            String joinDate = "";
            if (customer.createdAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                joinDate = customer.createdAt().format(formatter);
            }

            String genderStr = "Khác";
            if (customer.gender() != null) {
                genderStr = customer.gender() == Gender.MALE ? "Nam" : (customer.gender() == Gender.FEMALE ? "Nữ" : "Khác");
            }

            tableModel.addRow(new Object[] {
                customer.id(),
                customer.fullName(),
                customer.email() != null ? customer.email() : "",
                customer.phone() != null ? customer.phone() : "",
                genderStr,
                customer.isActive() != null && customer.isActive() ? "Hoạt động" : "Tạm khóa",
                joinDate
            });
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            String status = (value != null) ? value.toString() : "";
            
            JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            Color bg = "Hoạt động".equals(status) ? Theme.EMERALD : Theme.SLATE;
            Color fg = Theme.TEXT_WHITE;
            
            BadgeLabel label = new BadgeLabel(status, bg, fg);
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
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}
