package com.salonnbooking.api.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

import com.salonnbooking.domain.AppointmentStatus;

import jakarta.validation.constraints.NotNull;

public final class AppointmentRequests {
	private AppointmentRequests() {
	}

	public record Create(
			@NotNull Integer customerId,
			@NotNull List<Integer> serviceIds,
			Integer roomId,
			@NotNull LocalDateTime appointmentTime,
			AppointmentStatus status,
			String note) {
	}

	public record Update(
			@NotNull Integer customerId,
			@NotNull List<Integer> serviceIds,
			Integer roomId,
			@NotNull LocalDateTime appointmentTime,
			@NotNull AppointmentStatus status,
			String note) {
	}

	public record Response(
			Integer id,
			Integer customerId,
			List<Integer> serviceIds,
			Integer roomId,
			String roomName,
			LocalDateTime appointmentTime,
			BigDecimal totalAmount,
			BigDecimal depositAmount,
			BigDecimal amountPaid,
			BigDecimal remainingAmount,
			AppointmentStatus status,
			String note) {
		public static Response from(com.salonnbooking.domain.Appointment a) {
			return new Response(
					a.getId(),
					a.getCustomer().getId(),
					List.of(a.getService().getId()),
					a.getRoom() == null ? null : a.getRoom().getId(),
					a.getRoom() == null ? "" : a.getRoom().getName(),
					a.getAppointmentTime(),
					a.getTotalAmount(),
					a.getDepositAmount(),
					a.getAmountPaid(),
					a.getRemainingAmount(),
					a.getStatus(),
					a.getNote());
		}
	}
}
