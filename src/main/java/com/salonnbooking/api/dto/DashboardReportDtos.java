package com.salonnbooking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public final class DashboardReportDtos {

    private DashboardReportDtos() {
    }

    public record DashboardSummaryResponse(
            BigDecimal todayRevenue,
            long todayAppointments,
            long pendingAppointments,
            long confirmedAppointments,
            long completedAppointments,
            long cancelledAppointments) {
    }

    public record TopServiceResponse(
            Long serviceId,
            String serviceName,
            long bookingCount,
            BigDecimal revenue) {
    }

    public record TopStaffResponse(
            Long staffId,
            String staffName,
            long appointmentCount,
            BigDecimal revenue) {
    }

    public record DailyRevenueResponse(
            LocalDate date,
            BigDecimal revenue,
            long paidCount) {
    }

    public record MonthlyRevenueResponse(
            YearMonth month,
            BigDecimal revenue,
            long paidCount) {
    }
}
