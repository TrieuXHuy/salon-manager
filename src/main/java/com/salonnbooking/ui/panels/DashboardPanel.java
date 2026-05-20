package com.salonnbooking.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.Dimension;
import java.awt.Color;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.components.MetricCard;
import com.salonnbooking.ui.components.RevenueChartPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setBackground(Theme.BG_MAIN);
        
        // Page spacing: 30px padding, 20px gap
        setLayout(new MigLayout("wrap 4, insets 30, gap 20, fill", "[fill, 25%][fill, 25%][fill, 25%][fill, 25%]", "[100!][grow]"));

        // 1. Metric Cards (Hover effects, soft shadows, custom icons)
        MetricCard revenueCard = new MetricCard(
            "Doanh thu hôm nay", 
            "2.5M VNĐ", 
            "💵", 
            new Color(209, 250, 229), // Light emerald
            Theme.EMERALD
        );
        MetricCard appointmentsCard = new MetricCard(
            "Tổng lịch hẹn hôm nay", 
            "12 Lượt", 
            "📅", 
            new Color(219, 234, 254), // Light blue
            Theme.BLUE
        );
        MetricCard popularServiceCard = new MetricCard(
            "Dịch vụ thịnh hành", 
            "Uốn tóc Hàn Quốc", 
            "💇‍♂️", 
            new Color(254, 243, 199), // Light amber
            Theme.AMBER
        );
        MetricCard topEmployeeCard = new MetricCard(
            "Nhân viên xuất sắc", 
            "Nguyễn Minh (Stylist)", 
            "⭐", 
            new Color(251, 207, 232), // Light pink
            new Color(219, 39, 119) // Dark pink
        );

        add(revenueCard);
        add(appointmentsCard);
        add(popularServiceCard);
        add(topEmployeeCard);

        // 2. Revenue Chart Panel (Left - Span 2 columns)
        RoundedPanel chartContainer = new RoundedPanel(16, Theme.BG_CARD, true);
        chartContainer.setLayout(new MigLayout("insets 24, fill", "[fill]", "[][grow]"));
        
        JLabel chartTitle = new JLabel("Doanh thu tuần này (Triệu VNĐ)");
        chartTitle.setFont(Theme.FONT_H2);
        chartTitle.setForeground(Theme.NAVY);
        chartContainer.add(chartTitle, "wrap, gapbottom 10");

        RevenueChartPanel chartPanel = new RevenueChartPanel();
        chartContainer.add(chartPanel, "grow");

        add(chartContainer, "span 2, grow");

        // 3. Recent Appointments Table Panel (Right - Span 2 columns)
        RoundedPanel tablePanel = new RoundedPanel(16, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 24, fill", "[fill]", "[][grow]"));
        
        JLabel tableTitle = new JLabel("Lịch hẹn sắp tới");
        tableTitle.setFont(Theme.FONT_H2);
        tableTitle.setForeground(Theme.NAVY);
        tablePanel.add(tableTitle, "wrap, gapbottom 10");

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
        table.setRowHeight(45);
        table.setFont(Theme.FONT_BODY_REG);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setSelectionBackground(new Color(241, 245, 249)); // Soft selection color
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
        
        add(tablePanel, "span 2, grow");
    }
}
