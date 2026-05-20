package com.salonnbooking.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Cursor;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class ServicePanel extends JPanel {
    public ServicePanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][fill, grow]"));

        // 1. Filter Bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm dịch vụ...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

        // Category Filter
        String[] categories = {"Tất cả danh mục", "Cắt & Tạo kiểu", "Nhuộm & Uốn", "Chăm sóc tóc", "Dịch vụ khác"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        filterBar.add(categoryCombo, "gapright 20");

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

        String[] columns = {"Tên dịch vụ", "Danh mục", "Thời lượng", "Đơn giá", "Trạng thái"};
        Object[][] data = {
            {"Cắt tóc nam (bao gồm gội)", "Cắt & Tạo kiểu", "30 phút", "150,000đ", "Hoạt động"},
            {"Cắt tóc nữ & Tạo kiểu", "Cắt & Tạo kiểu", "45 phút", "250,000đ", "Hoạt động"},
            {"Uốn tóc nữ kiểu Hàn Quốc", "Nhuộm & Uốn", "120 phút", "800,000đ", "Hoạt động"},
            {"Nhuộm màu thời trang (L'Oreal)", "Nhuộm & Uốn", "90 phút", "650,000đ", "Hoạt động"},
            {"Gội đầu dưỡng sinh thảo dược", "Chăm sóc tóc", "45 phút", "200,000đ", "Hoạt động"},
            {"Phục hồi tóc hư tổn Olaplex", "Chăm sóc tóc", "60 phút", "500,000đ", "Hoạt động"},
            {"Nối mi Volume tự nhiên", "Dịch vụ khác", "75 phút", "350,000đ", "Ngưng hoạt động"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
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
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        add(tablePanel, "grow");
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
            
            Color bg;
            Color fg = Theme.TEXT_WHITE;
            
            if ("Hoạt động".equals(status) || "ACTIVE".equals(status)) {
                bg = Theme.EMERALD;
            } else {
                bg = Theme.SLATE;
            }
            
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
