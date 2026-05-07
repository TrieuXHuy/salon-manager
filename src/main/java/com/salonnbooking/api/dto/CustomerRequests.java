package com.salonnbooking.api.dto;

import com.salonnbooking.domain.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CustomerRequests {
	private CustomerRequests() {
	}

	public record Create(
			@NotBlank @Size(max = 100) String fullName,
			@NotBlank @Size(max = 20) String phone,
			@Email @Size(max = 100) String email,
			Gender gender) {
	}

	public record Update(
			@NotBlank @Size(max = 100) String fullName,
			@NotBlank @Size(max = 20) String phone,
			@Email @Size(max = 100) String email,
			Gender gender) {
	}

	public record Response(
			Integer id,
			String fullName,
			String phone,
			String email,
			Gender gender) {
		public static Response from(com.salonnbooking.domain.Customer c) {
			return new Response(c.getId(), c.getFullName(), c.getPhone(), c.getEmail(), c.getGender());
		}
	}
}
