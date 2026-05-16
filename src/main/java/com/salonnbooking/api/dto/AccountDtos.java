package com.salonnbooking.api.dto;

import java.time.LocalDateTime;

import com.salonnbooking.domain.Gender;
import com.salonnbooking.domain.Role;

public final class AccountDtos {

    private AccountDtos() {
    }

    public record ProfileResponse(
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

    public record UpdateProfileRequest(
            String fullName,
            String email,
            String phone,
            Gender gender) {
    }

    public record ChangePasswordRequest(
            String currentPassword,
            String newPassword) {
    }
}
