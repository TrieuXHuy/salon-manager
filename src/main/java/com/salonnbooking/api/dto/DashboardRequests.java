package com.salonnbooking.api.dto;

import java.math.BigDecimal;

public final class DashboardRequests {
	private DashboardRequests() {
	}

	public record DashboardResponse(
			Integer totalCustomers,
			Integer totalAppointmentsToday,
			Integer pendingAppointments,
			BigDecimal todayRevenue,
			BigDecimal monthlyRevenue,
			Double appointmentCompletionRate,
			Integer topServiceId,
			String topServiceName) {
	}

	public record QuickStatsResponse(
			Integer newCustomersThisMonth,
			Integer appointmentsThisMonth,
			BigDecimal revenueThisMonth,
			Double monthlyGrowthRate) {
	}
}
