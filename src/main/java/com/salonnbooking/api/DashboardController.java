package com.salonnbooking.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.DashboardRequests;
import com.salonnbooking.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public DashboardRequests.DashboardResponse getDashboard() {
		return dashboardService.getDashboard();
	}

	@GetMapping("/quick-stats")
	@ResponseStatus(HttpStatus.OK)
	public DashboardRequests.QuickStatsResponse getQuickStats() {
		return dashboardService.getQuickStats();
	}
}
