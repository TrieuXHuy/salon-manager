package com.salonnbooking.api.dto;

import java.time.LocalDateTime;

import com.salonnbooking.domain.Gender;
import com.salonnbooking.domain.Role;

public final class AdminUserDtos {

    private AdminUserDtos() {
    }

    public record CreateStaffRequest(
            String fullName,
            String email,
            String phone,
            String password,
            Gender gender,
            Boolean isActive) {
    }

    public record UpdateUserRequest(
            String fullName,
            String email,
            String phone,
            Gender gender,
            Boolean isActive) {
    }

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            Gender gender,
            Role role,
            Boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }
}
