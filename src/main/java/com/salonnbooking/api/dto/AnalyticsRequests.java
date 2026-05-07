package com.salonnbooking.api.dto;

import java.time.LocalDate;

public final class AnalyticsRequests {
	private AnalyticsRequests() {
	}

	public record CustomerAnalyticsResponse(
			Integer totalCustomers,
			Integer newCustomersThisMonth,
			Integer activeCustomers,
			Integer inactiveCustomers,
			Double customerRetentionRate) {
	}

	public record AppointmentTrendResponse(
			LocalDate date,
			Integer appointmentCount,
			Integer completedCount,
			Integer cancelledCount) {
	}

	public record ServicePerformanceResponse(
			Integer serviceId,
			String serviceName,
			Integer bookingCount,
			Double popularity,
			Double rating) {
	}

	public record PeakHoursResponse(
			String hour,
			Integer appointmentCount) {
	}
}
