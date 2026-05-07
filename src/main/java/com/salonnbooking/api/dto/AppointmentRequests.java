package com.salonnbooking.api.dto;

import java.time.LocalDateTime;

import com.salonnbooking.domain.AppointmentStatus;

import jakarta.validation.constraints.NotNull;

public final class AppointmentRequests {
	private AppointmentRequests() {
	}

	public record Create(
			@NotNull Integer customerId,
			@NotNull Integer serviceId,
			@NotNull LocalDateTime appointmentTime,
			AppointmentStatus status,
			String note) {
	}

	public record Update(
			@NotNull Integer customerId,
			@NotNull Integer serviceId,
			@NotNull LocalDateTime appointmentTime,
			@NotNull AppointmentStatus status,
			String note) {
	}

	public record Response(
			Integer id,
			Integer customerId,
			Integer serviceId,
			LocalDateTime appointmentTime,
			AppointmentStatus status,
			String note) {
		public static Response from(com.salonnbooking.domain.Appointment a) {
			return new Response(
					a.getId(),
					a.getCustomer().getId(),
					a.getService().getId(),
					a.getAppointmentTime(),
					a.getStatus(),
					a.getNote());
		}
	}
}
