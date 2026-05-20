package com.salonnbooking.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Cursor;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class CustomerPanel extends JPanel {
    public CustomerPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("insets 30, fill, wrap 1", "[fill]", "[][fill, grow]"));

        // 1. Filter and Action Bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0, fillx", "[][grow]push[]"));
        filterBar.setOpaque(false);

        // Search Field
        JTextField searchField = new JTextField(24);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên, số điện thoại...");
        filterBar.add(new JLabel("Tìm kiếm:"), "gapright 8");
        filterBar.add(searchField, "gapright 20");

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

        String[] columns = {"Họ và tên", "Số điện thoại", "Tổng số lượt đặt", "Đã chi tiêu", "Lần ghé thăm cuối"};
        Object[][] data = {
            {"Nguyễn Văn A", "0901 234 567", "12 lượt", "1,850,000đ", "20/05/2026"},
            {"Trần Thị B", "0987 654 321", "24 lượt", "6,400,000đ", "19/05/2026"},
            {"Lê Văn M", "0912 345 678", "3 lượt", "450,000đ", "15/05/2026"},
            {"Phạm Hoàng Nam", "0933 111 222", "8 lượt", "1,600,000đ", "20/05/2026"},
            {"Hoàng Thu Trang", "0977 444 555", "15 lượt", "3,150,000đ", "10/05/2026"},
            {"Đặng Minh Quân", "0905 888 999", "1 lượt", "180,000đ", "18/05/2026"},
            {"Vũ Anh Thư", "0944 555 666", "19 lượt", "4,900,000đ", "14/05/2026"}
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

        // Aligning columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        add(tablePanel, "grow");
    }
}
