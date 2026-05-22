package com.salonnbooking.api.dto;

import com.salonnbooking.domain.ServiceRoom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ServiceRoomRequests {
	private ServiceRoomRequests() {
	}

	public record Create(
			@NotBlank @Size(max = 100) String name,
			String description,
			Boolean isActive) {
	}

	public record Update(
			@NotBlank @Size(max = 100) String name,
			String description,
			Boolean isActive) {
	}

	public record Response(
			Integer id,
			String name,
			String description,
			Boolean isActive) {
		public static Response from(ServiceRoom room) {
			return new Response(room.getId(), room.getName(), room.getDescription(), room.getIsActive());
		}
	}
}
