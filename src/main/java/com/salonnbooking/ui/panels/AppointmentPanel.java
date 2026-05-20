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

public class AppointmentPanel extends JPanel {
    public AppointmentPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][fill, grow]"));

        // 1. Top filter and action bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        JTextField searchField = new JTextField(22);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm khách hàng, dịch vụ...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

        // Status Filter
        String[] statuses = {"Tất cả trạng thái", "Chờ xác nhận", "Đã xác nhận", "Đã hoàn thành", "Đã hủy"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        filterBar.add(statusCombo, "gapright 20");

        // Add Button (Primary/Emerald)
        JButton addButton = new JButton("+ Đặt lịch mới");
        addButton.setFont(Theme.FONT_H3);
        addButton.setForeground(Theme.TEXT_WHITE);
        addButton.setBackground(Theme.EMERALD);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Simple hover listener handled by FlatLaf but custom style can be set if needed
        filterBar.add(addButton, "h 38!");

        add(filterBar, "gapbottom 15");

        // 2. Appointments Table Container
        RoundedPanel tablePanel = new RoundedPanel(12, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 24, fill", "[fill]", "[grow]"));

        String[] columns = {"Mã LH", "Khách hàng", "Dịch vụ", "Nhân viên", "Thời gian", "Giá tiền", "Trạng thái"};
        Object[][] data = {
            {"LH-001", "Nguyễn Văn A", "Cắt tóc nam", "Trần Bình", "10:00 - Hôm nay", "150,000đ", "Đã xác nhận"},
            {"LH-002", "Trần Thị B", "Nhuộm tóc + Phục hồi", "Lê Thảo", "11:30 - Hôm nay", "1,200,000đ", "Đã hoàn thành"},
            {"LH-003", "Lê Văn M", "Gội đầu thảo dược", "Phạm Huy", "14:00 - Hôm nay", "200,000đ", "Chờ xác nhận"},
            {"LH-004", "Phạm Hoàng Nam", "Uốn tóc kiểu Hàn", "Trần Bình", "16:00 - Hôm nay", "450,000đ", "Đã xác nhận"},
            {"LH-005", "Hoàng Thu Trang", "Nối mi volume", "Nguyễn Mai", "09:00 - Ngày mai", "350,000đ", "Chờ xác nhận"},
            {"LH-006", "Đặng Minh Quân", "Cạo mặt + Massage", "Phạm Huy", "10:30 - Ngày mai", "180,000đ", "Đã hủy"}
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

        // Apply Custom Status Renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        // Align details
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        add(tablePanel, "grow");
    }

    // Custom status renderer displaying rounded badges
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
            
            switch (status) {
                case "Đã hoàn thành":
                    bg = Theme.EMERALD;
                    break;
                case "Đã xác nhận":
                    bg = Theme.BLUE;
                    break;
                case "Chờ xác nhận":
                    bg = Theme.AMBER;
                    break;
                case "Đã hủy":
                    bg = Theme.CRIMSON;
                    break;
                default:
                    bg = Theme.SLATE;
                    break;
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
