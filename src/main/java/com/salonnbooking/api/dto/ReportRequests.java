package com.salonnbooking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReportRequests {
	private ReportRequests() {
	}

	public record DailyRevenueResponse(
			LocalDate date,
			BigDecimal totalRevenue,
			Integer appointmentCount,
			Integer completedCount) {
	}

	public record ServiceRevenueResponse(
			Integer serviceId,
			String serviceName,
			Integer appointmentCount,
			BigDecimal totalRevenue,
			BigDecimal avgRevenue) {
	}

	public record PaymentMethodResponse(
			String paymentMethod,
			Integer count,
			BigDecimal totalAmount,
			BigDecimal percentage) {
	}

	public record AppointmentStatsResponse(
			Integer totalAppointments,
			Integer pendingAppointments,
			Integer confirmedAppointments,
			Integer completedAppointments,
			Integer cancelledAppointments) {
	}

	public record DateRangeRevenueRequest(
			LocalDate startDate,
			LocalDate endDate) {
	}
}
