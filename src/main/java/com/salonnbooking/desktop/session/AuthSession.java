package com.salonnbooking.desktop.session;

import com.salonnbooking.desktop.model.Role;

public final class AuthSession {

    private static final AuthSession INSTANCE = new AuthSession();

    private String token;
    private Long userId;
    private String fullName;
    private Role role;

    private AuthSession() {
    }

    public static AuthSession getInstance() {
        return INSTANCE;
    }

    public boolean isAuthenticated() {
        return token != null && !token.isBlank();
    }

    public void clear() {
        token = null;
        userId = null;
        fullName = null;
        role = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
