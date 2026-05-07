package com.salonnbooking.api.dto;

import com.salonnbooking.domain.SmsStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SmsLogRequests {
	private SmsLogRequests() {
	}

	public record Create(
			@NotNull Integer appointmentId,
			@NotBlank @Size(max = 20) String phone,
			@NotBlank String message,
			SmsStatus status) {
	}

	public record Update(
			@NotNull Integer appointmentId,
			@NotBlank @Size(max = 20) String phone,
			@NotBlank String message,
			@NotNull SmsStatus status) {
	}

	public record Response(
			Integer id,
			Integer appointmentId,
			String phone,
			String message,
			SmsStatus status) {
		public static Response from(com.salonnbooking.domain.SmsLog s) {
			return new Response(
					s.getId(),
					s.getAppointment().getId(),
					s.getPhone(),
					s.getMessage(),
					s.getStatus());
		}
	}
}
