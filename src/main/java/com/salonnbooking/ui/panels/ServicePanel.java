package com.salonnbooking.ui.panels;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.util.JsonUtil;
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
import java.util.ArrayList;
import java.util.List;

public class ServicePanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";
    private final ApiClient apiClient = new ApiClient(BASE_URL);

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> categoryCombo;
    private final JButton refreshBtn;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private List<ServiceDtos.Response> allServices = new ArrayList<>();

    public ServicePanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][fill, grow]"));

        // 1. Filter Bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm dịch vụ...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

        // Category Filter
        String[] categories = {"Tất cả danh mục", "Cắt & Tạo kiểu", "Nhuộm & Uốn", "Chăm sóc tóc", "Dịch vụ khác"};
        categoryCombo = new JComboBox<>(categories);
        filterBar.add(categoryCombo, "gapright 20");

        // Refresh Button
        refreshBtn = new JButton("Làm mới");
        refreshBtn.setFont(Theme.FONT_BODY_LG);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBar.add(refreshBtn, "h 38!, gapright 10");

        // Add Service Button
        JButton addButton = new JButton("+ Thêm dịch vụ");
        addButton.setFont(Theme.FONT_H3);
        addButton.setForeground(Theme.TEXT_WHITE);
        addButton.setBackground(Theme.EMERALD);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBar.add(addButton, "h 38!");

        add(filterBar, "gapbottom 15");

        // 2. Services Table
        RoundedPanel tablePanel = new RoundedPanel(12, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 24, fill", "[fill]", "[grow]"));

        String[] columns = {"ID", "Tên dịch vụ", "Danh mục", "Thời lượng", "Đơn giá", "Trạng thái"};
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
        table.setSelectionBackground(new Color(219, 242, 241));
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

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        add(tablePanel, "grow");

        // Listeners for filters
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterAndRenderTable();
            }
        });
        categoryCombo.addActionListener(e -> filterAndRenderTable());
        refreshBtn.addActionListener(e -> loadServices());

        // Initial Load
        loadServices();
    }

    private void setLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        refreshBtn.setEnabled(!loading);
        searchField.setEnabled(!loading);
        categoryCombo.setEnabled(!loading);
    }

    private void loadServices() {
        setLoading(true);
        SwingWorker<List<ServiceDtos.Response>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceDtos.Response> doInBackground() throws Exception {
                // Skip API call in mock mode
                if (com.salonnbooking.desktop.session.AuthSession.getInstance().isMockSession()) {
                    return createMockData();
                }
                try {
                    String json = apiClient.getRaw("/api/admin/services");
                    Type type = new TypeToken<List<ServiceDtos.Response>>() {}.getType();
                    return JsonUtil.fromJson(json, type);
                } catch (Exception ex) {
                    System.out.println("[API Offline] Tải danh sách dịch vụ thất bại, kích hoạt Mock Data: " + ex.getMessage());
                    return createMockData();
                }
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    allServices = get();
                    filterAndRenderTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ServicePanel.this, 
                        "Lỗi hiển thị dịch vụ: " + ex.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void filterAndRenderTable() {
        tableModel.setRowCount(0);
        String query = searchField.getText().trim().toLowerCase();
        int categoryIndex = categoryCombo.getSelectedIndex();
        String selectedCategory = categoryIndex > 0 ? categoryCombo.getSelectedItem().toString() : "";

        for (ServiceDtos.Response svc : allServices) {
            // Search filters
            boolean matchesSearch = query.isEmpty() ||
                (svc.name() != null && svc.name().toLowerCase().contains(query)) ||
                (svc.description() != null && svc.description().toLowerCase().contains(query));

            if (!matchesSearch) continue;

            // Category filters
            boolean matchesCategory = selectedCategory.isEmpty() || 
                (svc.categoryName() != null && svc.categoryName().equals(selectedCategory));

            if (!matchesCategory) continue;

            // Format price
            String priceStr = "0đ";
            if (svc.price() != null) {
                java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                priceStr = nf.format(svc.price()) + "đ";
            }

            String durationStr = svc.durationMinutes() + " phút";
            String statusStr = svc.isActive() != null && svc.isActive() ? "Hoạt động" : "Ngưng hoạt động";

            tableModel.addRow(new Object[] {
                svc.id(),
                svc.name(),
                svc.categoryName() != null ? svc.categoryName() : "Khác",
                durationStr,
                priceStr,
                statusStr
            });
        }
    }

    private List<ServiceDtos.Response> createMockData() {
        List<ServiceDtos.Response> list = new ArrayList<>();
        list.add(new ServiceDtos.Response(
            10L, 1L, "Cắt & Tạo kiểu", "Cắt tóc nam (bao gồm gội)", "Cắt tóc cơ bản và gội đầu thư giãn", 
            new BigDecimal("150000"), 30, true, java.time.LocalDateTime.now()
        ));
        list.add(new ServiceDtos.Response(
            11L, 1L, "Cắt & Tạo kiểu", "Cắt tóc nữ & Tạo kiểu", "Tạo kiểu layer, uốn lọn nhẹ", 
            new BigDecimal("250000"), 45, true, java.time.LocalDateTime.now()
        ));
        list.add(new ServiceDtos.Response(
            12L, 2L, "Nhuộm & Uốn", "Uốn tóc nữ kiểu Hàn Quốc", "Uốn sóng nước, uốn cụp cụ thể", 
            new BigDecimal("800000"), 120, true, java.time.LocalDateTime.now()
        ));
        list.add(new ServiceDtos.Response(
            13L, 2L, "Nhuộm & Uốn", "Nhuộm màu thời trang (L'Oreal)", "Tẩy tóc, nhuộm khói, xám tro", 
            new BigDecimal("650000"), 90, true, java.time.LocalDateTime.now()
        ));
        list.add(new ServiceDtos.Response(
            14L, 3L, "Chăm sóc tóc", "Gội đầu dưỡng sinh thảo dược", "Massage đầu, gội sạch bằng dầu thảo dược", 
            new BigDecimal("200000"), 45, true, java.time.LocalDateTime.now()
        ));
        list.add(new ServiceDtos.Response(
            15L, 3L, "Chăm sóc tóc", "Phục hồi tóc hư tổn Olaplex", "Ủ dưỡng chất phục hồi tóc xơ", 
            new BigDecimal("500000"), 60, true, java.time.LocalDateTime.now()
        ));
        list.add(new ServiceDtos.Response(
            16L, 4L, "Dịch vụ khác", "Nối mi Volume tự nhiên", "Nối mi dầy, uốn mi cong", 
            new BigDecimal("350000"), 75, false, java.time.LocalDateTime.now()
        ));
        return list;
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            String status = (value != null) ? value.toString() : "";
            
            JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 8));
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
