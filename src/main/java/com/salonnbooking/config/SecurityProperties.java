package com.salonnbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        Token token,
        BootstrapAdmin bootstrapAdmin) {

    public record Token(long expiryDays, String headerName) {
    }

    public record BootstrapAdmin(
            boolean enabled,
            String fullName,
            String email,
            String password,
            String phone) {
    }
}
