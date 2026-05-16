package com.salonnbooking.desktop.model;

import java.time.LocalDateTime;

public final class AuthModels {

    private AuthModels() {
    }

    public record RegisterRequest(
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

    public record AuthResponse(
            String token,
            ProfileResponse user) {
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
