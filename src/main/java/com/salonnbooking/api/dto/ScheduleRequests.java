package com.salonnbooking.api.dto;

import java.time.LocalDateTime;

import com.salonnbooking.domain.AppointmentStatus;

public final class ScheduleRequests {
	private ScheduleRequests() {
	}

	public record AvailableSlotResponse(
			LocalDateTime slotTime,
			Boolean isAvailable,
			Integer serviceId,
			Integer roomId,
			String roomName,
			Integer durationMinutes,
			Integer bookedSlots,
			Integer totalSlots) {
	}

	public record AppointmentScheduleResponse(
			Integer appointmentId,
			Integer customerId,
			String customerName,
			Integer serviceId,
			String serviceName,
			Integer roomId,
			String roomName,
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
