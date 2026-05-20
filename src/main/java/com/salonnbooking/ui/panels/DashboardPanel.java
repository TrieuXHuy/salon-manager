package com.salonnbooking.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Color;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setBackground(Theme.BG_MAIN);
        // Using standard spacing from DESIGN_SYSTEM.md: page padding 30px, card gaps 20px
        setLayout(new MigLayout("wrap 4, insets 30, gap 20, fillx", "[fill, 25%][fill, 25%][fill, 25%][fill, 25%]"));

        // 1. Stats Cards (12px border radius, subtle shadows)
        add(createStatCard("Lịch hẹn hôm nay", "12", Theme.NAVY), "h 120!");
        add(createStatCard("Khách hàng mới", "5", Theme.EMERALD), "h 120!");
        add(createStatCard("Doanh thu ngày", "2.5M", Theme.AMBER), "h 120!");
        add(createStatCard("Đang phục vụ", "3", Theme.BLUE), "h 120!");

        // 2. Recent Appointments Table Panel
        RoundedPanel tablePanel = new RoundedPanel(12, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 24, fill", "[fill]", "[][fill, grow]"));
        
        JLabel tableTitle = new JLabel("Lịch hẹn sắp tới");
        tableTitle.setFont(Theme.FONT_H2);
        tableTitle.setForeground(Theme.NAVY);
        tablePanel.add(tableTitle, "wrap, gapbottom 12");

        // Columns & Data
        String[] columns = {"Khách hàng", "Dịch vụ", "Thời gian", "Trạng thái"};
        Object[][] data = {
            {"Nguyễn Văn A", "Cắt tóc nam", "10:00 - Hôm nay", "Đã xác nhận"},
            {"Trần Thị B", "Nhuộm tóc + Phục hồi", "11:30 - Hôm nay", "Khách đã đến"},
            {"Lê Văn M", "Gội đầu thảo dược", "14:00 - Hôm nay", "Chờ xác nhận"},
            {"Phạm Hoàng Nam", "Uốn tóc kiểu Hàn", "16:00 - Hôm nay", "Đã xác nhận"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        
        // Table styling matching DESIGN_SYSTEM.md (Row height 45px, clean borders)
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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        
        add(tablePanel, "span 4, growy, pushy, gapt 10");
    }

    private RoundedPanel createStatCard(String title, String value, Color color) {
        RoundedPanel card = new RoundedPanel(12, Theme.BG_CARD, true);
        card.setLayout(new MigLayout("insets 20, fillx", "[left]"));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_BODY_LG);
        titleLabel.setForeground(Theme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.FONT_HERO.deriveFont(28f));
        valueLabel.setForeground(color);

        card.add(titleLabel, "wrap, gapbottom 8");
        card.add(valueLabel);
        return card;
    }
}
