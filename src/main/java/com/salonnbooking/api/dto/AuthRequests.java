package com.salonnbooking.api.dto;

import com.salonnbooking.domain.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthRequests {
	private AuthRequests() {
	}

	public record Login(
			@NotBlank @Size(max = 50) String username,
			@NotBlank @Size(max = 255) String password) {
	}

	public record Register(
			@NotBlank @Size(max = 50) String username,
			@NotBlank @Size(max = 255) String password) {
	}

	public record Response(
			Integer id,
			String username,
			String message) {
		public static Response from(User user, String message) {
			return new Response(user.getId(), user.getUsername(), message);
		}
	}

	public record Message(String message) {
	}
}
