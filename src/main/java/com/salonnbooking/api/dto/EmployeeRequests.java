package com.salonnbooking.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EmployeeRequests {
	private EmployeeRequests() {
	}

	public record Create(
			@Size(max = 30) String employeeCode,
			@NotBlank @Size(max = 255) String fullName,
			@Size(max = 20) String phone,
			@Size(max = 255) String email,
			@Size(max = 255) String specialization,
			LocalDate hireDate,
			Boolean isActive) {
	}

	public record Update(
			@Size(max = 30) String employeeCode,
			@NotBlank @Size(max = 255) String fullName,
			@Size(max = 20) String phone,
			@Size(max = 255) String email,
			@Size(max = 255) String specialization,
			LocalDate hireDate,
			Boolean isActive) {
	}

	public record Response(
			Integer id,
			String employeeCode,
			String fullName,
			String phone,
			String email,
			String specialization,
			LocalDate hireDate,
			Boolean isActive) {
		public static Response from(com.salonnbooking.domain.Employee employee) {
			return new Response(
					employee.getId(),
					employee.getEmployeeCode(),
					employee.getFullName(),
					employee.getPhone(),
					employee.getEmail(),
					employee.getSpecialization(),
					employee.getHireDate(),
					employee.getIsActive());
		}
	}
}
