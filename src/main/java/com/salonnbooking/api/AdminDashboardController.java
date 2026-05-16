package com.salonnbooking.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.DashboardReportDtos;
import com.salonnbooking.service.DashboardReportService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardReportService dashboardReportService;

    public AdminDashboardController(DashboardReportService dashboardReportService) {
        this.dashboardReportService = dashboardReportService;
    }

    @GetMapping("/summary")
    public DashboardReportDtos.DashboardSummaryResponse getSummary() {
        return dashboardReportService.getSummary();
    }
}
