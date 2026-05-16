package com.salonnbooking.api.dto;

import java.time.LocalDateTime;

import com.salonnbooking.domain.Gender;
import com.salonnbooking.domain.Role;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            String fullName,
            String email,
            String phone,
            String password,
            Gender gender) {
    }

    public record RegisterAdminRequest(
            String fullName,
            String email,
            String phone,
            String password,
            Gender gender) {
    }

    public record LoginRequest(
            String email,
            String password) {
    }

    public record AuthResponse(
            String token,
            AccountDtos.ProfileResponse user) {
    }

    public record MeResponse(
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
