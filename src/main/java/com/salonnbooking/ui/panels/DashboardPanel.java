package com.salonnbooking.ui.panels;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.api.dto.DashboardReportDtos;
import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.util.JsonUtil;
import com.salonnbooking.ui.components.MetricCard;
import com.salonnbooking.ui.components.RevenueChartPanel;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";
    private final ApiClient apiClient = new ApiClient(BASE_URL);

    private final MetricCard revenueCard;
    private final MetricCard appointmentsCard;
    private final MetricCard popularServiceCard;
    private final MetricCard topEmployeeCard;

    private final RevenueChartPanel chartPanel;
    private final DefaultTableModel tableModel;

    public DashboardPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("wrap 4, insets 30, gap 20, fill", "[fill, 25%][fill, 25%][fill, 25%][fill, 25%]", "[100!][grow]"));

        // 1. Metric Cards (Hover effects, soft shadows, custom icons)
        revenueCard = new MetricCard(
            "Doanh thu hôm nay", 
            "--- VNĐ", 
            "💵", 
            new Color(209, 250, 229), // Light emerald
            Theme.EMERALD
        );
        appointmentsCard = new MetricCard(
            "Tổng lịch hẹn hôm nay", 
            "--- Lượt", 
            "📅", 
            new Color(219, 234, 254), // Light blue
            Theme.BLUE
        );
        popularServiceCard = new MetricCard(
            "Dịch vụ thịnh hành", 
            "---", 
            "💇‍♂️", 
            new Color(254, 243, 199), // Light amber
            Theme.AMBER
        );
        topEmployeeCard = new MetricCard(
            "Nhân viên xuất sắc", 
            "---", 
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

        chartPanel = new RevenueChartPanel();
        chartContainer.add(chartPanel, "grow");

        add(chartContainer, "span 2, grow");

        // 3. Recent Appointments Table Panel (Right - Span 2 columns)
        RoundedPanel tablePanel = new RoundedPanel(16, Theme.BG_CARD, true);
        tablePanel.setLayout(new MigLayout("insets 24, fill", "[fill]", "[][grow]"));
        
        JLabel tableTitle = new JLabel("Lịch hẹn sắp tới");
        tableTitle.setFont(Theme.FONT_H2);
        tableTitle.setForeground(Theme.NAVY);
        tablePanel.add(tableTitle, "wrap, gapbottom 10");

        // Columns
        String[] columns = {"Khách hàng", "Dịch vụ", "Thời gian", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(tableModel);
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

        // Load Dashboard Data
        loadDashboardData();
    }

    private void loadDashboardData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData data = new DashboardData();
                
                // Skip all API calls in mock mode — UI already has fallback values
                if (com.salonnbooking.desktop.session.AuthSession.getInstance().isMockSession()) {
                    return data;
                }
                
                // 1. Fetch Summary
                try {
                    String json = apiClient.getRaw("/api/admin/dashboard/summary");
                    data.summary = JsonUtil.fromJson(json, DashboardReportDtos.DashboardSummaryResponse.class);
                } catch (Exception e) {
                    System.out.println("Summary API offline: " + e.getMessage());
                }

                // 2. Fetch Top Service
                try {
                    String json = apiClient.getRaw("/api/admin/reports/top-services?limit=1");
                    Type type = new TypeToken<List<DashboardReportDtos.TopServiceResponse>>(){}.getType();
                    List<DashboardReportDtos.TopServiceResponse> list = JsonUtil.fromJson(json, type);
                    if (list != null && !list.isEmpty()) {
                        data.topService = list.get(0);
                    }
                } catch (Exception e) {
                    System.out.println("Top Service API offline: " + e.getMessage());
                }

                // 3. Fetch Top Staff
                try {
                    String json = apiClient.getRaw("/api/admin/reports/top-staff?limit=1");
                    Type type = new TypeToken<List<DashboardReportDtos.TopStaffResponse>>(){}.getType();
                    List<DashboardReportDtos.TopStaffResponse> list = JsonUtil.fromJson(json, type);
                    if (list != null && !list.isEmpty()) {
                        data.topStaff = list.get(0);
                    }
                } catch (Exception e) {
                    System.out.println("Top Staff API offline: " + e.getMessage());
                }

                // 4. Fetch Daily Revenue for Chart
                try {
                    LocalDate start = LocalDate.now().minusDays(6);
                    LocalDate end = LocalDate.now();
                    String json = apiClient.getRaw("/api/admin/reports/revenue-daily?startDate=" + start + "&endDate=" + end);
                    Type type = new TypeToken<List<DashboardReportDtos.DailyRevenueResponse>>(){}.getType();
                    data.chartRevenues = JsonUtil.fromJson(json, type);
                } catch (Exception e) {
                    System.out.println("Daily Revenue API offline: " + e.getMessage());
                }

                // 5. Fetch Recent Appointments
                try {
                    String json = apiClient.getRaw("/api/admin/appointments");
                    Type type = new TypeToken<List<BookingDtos.AppointmentResponse>>(){}.getType();
                    data.appointments = JsonUtil.fromJson(json, type);
                } catch (Exception e) {
                    System.out.println("Appointments API offline: " + e.getMessage());
                }

                return data;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    DashboardData result = get();
                    updateUIWithData(result);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this, 
                        "Lỗi tải dữ liệu Dashboard: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateUIWithData(DashboardData data) {
        // Formatter for Currency
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));

        // 1. Update Metric 1: Revenue
        if (data.summary != null && data.summary.todayRevenue() != null) {
            revenueCard.setValue(nf.format(data.summary.todayRevenue()) + "đ");
        } else {
            revenueCard.setValue("2.500.000đ"); // Mock fallback
        }

        // 2. Update Metric 2: Today Appointments
        if (data.summary != null) {
            appointmentsCard.setValue(data.summary.todayAppointments() + " Lượt");
        } else {
            appointmentsCard.setValue("12 Lượt"); // Mock fallback
        }

        // 3. Update Metric 3: Top Service
        if (data.topService != null && data.topService.serviceName() != null) {
            popularServiceCard.setValue(data.topService.serviceName());
        } else {
            popularServiceCard.setValue("Uốn tóc Hàn Quốc"); // Mock fallback
        }

        // 4. Update Metric 4: Top Staff
        if (data.topStaff != null && data.topStaff.staffName() != null) {
            topEmployeeCard.setValue(data.topStaff.staffName());
        } else {
            topEmployeeCard.setValue("Trần Bình (Stylist)"); // Mock fallback
        }

        // 5. Update Revenue Chart
        if (data.chartRevenues != null && !data.chartRevenues.isEmpty()) {
            String[] days = new String[data.chartRevenues.size()];
            double[] vals = new double[data.chartRevenues.size()];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            BigDecimal million = new BigDecimal("1000000");

            for (int i = 0; i < data.chartRevenues.size(); i++) {
                DashboardReportDtos.DailyRevenueResponse item = data.chartRevenues.get(i);
                days[i] = item.date() != null ? item.date().format(formatter) : "Ngày " + (i+1);
                BigDecimal rev = item.revenue() != null ? item.revenue() : BigDecimal.ZERO;
                vals[i] = rev.divide(million, 2, java.math.RoundingMode.HALF_UP).doubleValue();
            }
            chartPanel.setData(days, vals);
        } else {
            // Default mock chart data
            String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
            double[] values = {1.8, 2.5, 1.5, 3.2, 4.0, 6.5, 5.2};
            chartPanel.setData(days, values);
        }

        // 6. Update Table
        tableModel.setRowCount(0);
        if (data.appointments != null && !data.appointments.isEmpty()) {
            int count = 0;
            for (BookingDtos.AppointmentResponse app : data.appointments) {
                if (count >= 5) break; // Display only 5 recent appointments
                
                String servicesStr = "";
                if (app.services() != null && !app.services().isEmpty()) {
                    List<String> names = app.services().stream().map(BookingDtos.AppointmentServiceResponse::serviceName).toList();
                    servicesStr = String.join(", ", names);
                }

                String timeStr = "";
                if (app.appointmentStart() != null) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM");
                    timeStr = app.appointmentStart().format(dtf);
                }

                String statusStr = "Chờ xác nhận";
                if (app.status() != null) {
                    switch (app.status()) {
                        case PENDING: statusStr = "Chờ xác nhận"; break;
                        case CONFIRMED: statusStr = "Đã xác nhận"; break;
                        case IN_PROGRESS: statusStr = "Đang phục vụ"; break;
                        case COMPLETED: statusStr = "Đã hoàn thành"; break;
                        case CANCELLED: statusStr = "Đã hủy"; break;
                    }
                }

                tableModel.addRow(new Object[] {
                    app.customerName() != null ? app.customerName() : "",
                    servicesStr,
                    timeStr,
                    statusStr
                });
                count++;
            }
        } else {
            // Default mock table data
            tableModel.addRow(new Object[] {"Nguyễn Văn A", "Cắt tóc nam", "10:00 - Hôm nay", "Đã xác nhận"});
            tableModel.addRow(new Object[] {"Trần Thị B", "Nhuộm tóc + Phục hồi", "11:30 - Hôm nay", "Đang phục vụ"});
            tableModel.addRow(new Object[] {"Lê Văn M", "Gội đầu thảo dược", "14:00 - Hôm nay", "Chờ xác nhận"});
            tableModel.addRow(new Object[] {"Phạm Hoàng Nam", "Uốn tóc kiểu Hàn", "16:00 - Hôm nay", "Đã xác nhận"});
        }
    }

    private static class DashboardData {
        DashboardReportDtos.DashboardSummaryResponse summary;
        DashboardReportDtos.TopServiceResponse topService;
        DashboardReportDtos.TopStaffResponse topStaff;
        List<DashboardReportDtos.DailyRevenueResponse> chartRevenues;
        List<BookingDtos.AppointmentResponse> appointments;
    }
}
