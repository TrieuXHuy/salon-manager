package com.salonnbooking.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class ServiceRequests {
	private ServiceRequests() {
	}

	public record Create(
			@NotBlank @Size(max = 100) String name,
			@NotNull @Positive BigDecimal price,
			@NotNull @Positive Integer durationMinutes,
			String description,
			Boolean isActive) {
	}

	public record Update(
			@NotBlank @Size(max = 100) String name,
			@NotNull @Positive BigDecimal price,
			@NotNull @Positive Integer durationMinutes,
			String description,
			Boolean isActive) {
	}

	public record Response(
			Integer id,
			String name,
			BigDecimal price,
			Integer durationMinutes,
			String description,
			Boolean isActive) {
		public static Response from(com.salonnbooking.domain.ServiceEntity s) {
			return new Response(s.getId(), s.getName(), s.getPrice(), s.getDurationMinutes(), s.getDescription(),
					s.getIsActive());
		}
	}
}
