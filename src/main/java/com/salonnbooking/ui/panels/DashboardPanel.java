package com.salonnbooking.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.Dimension;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("wrap 4, insets 30, gap 20, fillx", "[fill, 25%][fill, 25%][fill, 25%][fill, 25%]"));

        // 1. Stats Cards
        add(createStatCard("Lịch hẹn hôm nay", "12", Theme.PRIMARY), "h 120!");
        add(createStatCard("Khách hàng mới", "5", Theme.SUCCESS), "h 120!");
        add(createStatCard("Doanh thu ngày", "2.5M", Theme.WARNING), "h 120!");
        add(createStatCard("Đang phục vụ", "3", Theme.TEXT_MUTED), "h 120!");

        // 2. Recent Appointments Table
        RoundedPanel tablePanel = new RoundedPanel(15, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 20, fill", "[fill]", "[][fill, grow]"));
        
        JLabel tableTitle = new JLabel("Lịch hẹn sắp tới");
        tableTitle.setFont(Theme.FONT_H3);
        tableTitle.setForeground(Theme.TEXT_MAIN);
        tablePanel.add(tableTitle, "wrap");

        // Simple modern table
        String[] columns = {"Khách hàng", "Dịch vụ", "Thời gian", "Trạng thái"};
        Object[][] data = {
            {"Nguyễn Văn A", "Cắt tóc nam", "10:00 - Hôm nay", "Đã giữ chỗ"},
            {"Trần Thị B", "Nhuộm tóc + Phục hồi", "11:30 - Hôm nay", "Khách đã đến"},
            {"Lê Văn M", "Gội đầu", "14:00 - Hôm nay", "Chờ xác nhận"},
            {"Phạm N", "Cắt + Uốn", "16:00 - Hôm nay", "Đã giữ chỗ"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model);
        
        // Table styling
        table.setRowHeight(40);
        table.setFont(Theme.FONT_REGULAR);
        table.setForeground(Theme.TEXT_MAIN);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Theme.PRIMARY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_BOLD);
        header.setBackground(Theme.BG_MAIN);
        header.setForeground(Theme.TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 40));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);

        tablePanel.add(scrollPane, "grow");
        
        add(tablePanel, "span 4, growy, pushy, gapt 20");
    }

    private RoundedPanel createStatCard(String title, String value, java.awt.Color color) {
        RoundedPanel card = new RoundedPanel(15, Theme.BG_CARD, true);
        card.setLayout(new MigLayout("insets 20", "[left]"));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_MEDIUM);
        titleLabel.setForeground(Theme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.FONT_H1.deriveFont(28f));
        valueLabel.setForeground(color);

        card.add(titleLabel, "wrap");
        card.add(valueLabel);
        return card;
    }
}
