package com.salonnbooking.api.dto;

import com.salonnbooking.domain.User;
import com.salonnbooking.domain.UserRole;

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

	public record CreateUser(
			@NotBlank @Size(max = 50) String requesterUsername,
			@NotBlank @Size(max = 50) String username,
			@NotBlank @Size(max = 255) String password,
			UserRole role) {
	}

	public record UpdateUser(
			@NotBlank @Size(max = 50) String requesterUsername,
			@NotBlank @Size(max = 50) String username,
			String password,
			UserRole role) {
	}

	public record Response(
			Integer id,
			String username,
			UserRole role,
			String roleName,
			String message) {
		public static Response from(User user, String message) {
			return new Response(user.getId(), user.getUsername(), user.getRole(), user.getRole().getDisplayName(), message);
		}
	}

	public record UserResponse(
			Integer id,
			String username,
			UserRole role,
			String roleName) {
		public static UserResponse from(User user) {
			return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getRole().getDisplayName());
		}
	}

	public record ChangeRole(
			@NotBlank @Size(max = 50) String requesterUsername,
			UserRole role) {
	}

	public record Message(String message) {
	}
}
