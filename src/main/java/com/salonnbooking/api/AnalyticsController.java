package com.salonnbooking.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.AnalyticsRequests;
import com.salonnbooking.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/customers")
	@ResponseStatus(HttpStatus.OK)
	public AnalyticsRequests.CustomerAnalyticsResponse getCustomerAnalytics() {
		return analyticsService.getCustomerAnalytics();
	}

	@GetMapping("/appointment-trends")
	@ResponseStatus(HttpStatus.OK)
	public List<AnalyticsRequests.AppointmentTrendResponse> getAppointmentTrends(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return analyticsService.getAppointmentTrends(startDate, endDate);
	}

	@GetMapping("/service-performance")
	@ResponseStatus(HttpStatus.OK)
	public List<AnalyticsRequests.ServicePerformanceResponse> getServicePerformance() {
		return analyticsService.getServicePerformance();
	}

	@GetMapping("/peak-hours")
	@ResponseStatus(HttpStatus.OK)
	public List<AnalyticsRequests.PeakHoursResponse> getPeakHours() {
		return analyticsService.getPeakHours();
	}
}
