package com.salonnbooking.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.DashboardReportDtos;
import com.salonnbooking.service.DashboardReportService;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final DashboardReportService dashboardReportService;

    public AdminReportController(DashboardReportService dashboardReportService) {
        this.dashboardReportService = dashboardReportService;
    }

    @GetMapping("/top-services")
    public List<DashboardReportDtos.TopServiceResponse> getTopServices(
            @RequestParam(required = false) Integer limit) {
        return dashboardReportService.getTopServices(limit);
    }

    @GetMapping("/top-staff")
    public List<DashboardReportDtos.TopStaffResponse> getTopStaff(
            @RequestParam(required = false) Integer limit) {
        return dashboardReportService.getTopStaff(limit);
    }

    @GetMapping("/revenue-daily")
    public List<DashboardReportDtos.DailyRevenueResponse> getRevenueDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dashboardReportService.getRevenueDaily(startDate, endDate);
    }

    @GetMapping("/revenue-monthly")
    public List<DashboardReportDtos.MonthlyRevenueResponse> getRevenueMonthly(
            @RequestParam(required = false) Integer year) {
        return dashboardReportService.getRevenueMonthly(year);
    }
}
