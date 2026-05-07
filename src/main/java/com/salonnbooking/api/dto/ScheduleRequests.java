package com.salonnbooking.api.dto;

import java.time.LocalDateTime;

import com.salonnbooking.domain.AppointmentStatus;

public final class ScheduleRequests {
	private ScheduleRequests() {
	}

	public record AvailableSlotResponse(
			LocalDateTime slotTime,
			Boolean isAvailable,
			Integer serviceId) {
	}

	public record AppointmentScheduleResponse(
			Integer appointmentId,
			Integer customerId,
			String customerName,
			Integer serviceId,
			String serviceName,
			LocalDateTime appointmentTime,
			Integer durationMinutes,
			AppointmentStatus status,
			String note) {
	}

	public record DayScheduleResponse(
			LocalDateTime date,
			Integer totalSlots,
			Integer bookedSlots,
			Integer availableSlots) {
	}
}
