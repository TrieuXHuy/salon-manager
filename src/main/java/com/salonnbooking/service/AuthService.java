package com.salonnbooking.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AccountDtos;
import com.salonnbooking.api.dto.AuthDtos;
import com.salonnbooking.config.SecurityProperties;
import com.salonnbooking.domain.AuthToken;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.AuthTokenRepository;
import com.salonnbooking.repository.UserRepository;
import com.salonnbooking.security.CurrentUserService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final CurrentUserService currentUserService;

    public AuthService(
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder,
            SecurityProperties securityProperties,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AccountDtos.ProfileResponse registerCustomer(AuthDtos.RegisterRequest request) {
        User user = buildUser(
                request.fullName(),
                request.email(),
                request.phone(),
                request.password(),
                request.gender(),
                Role.CUSTOMER);

        return toProfile(userRepository.save(user));
    }

    @Transactional
    public AccountDtos.ProfileResponse registerAdmin(AuthDtos.RegisterAdminRequest request) {
        User user = buildUser(
                request.fullName(),
                request.email(),
                request.phone(),
                request.password(),
                request.gender(),
                Role.ADMIN);

        return toProfile(userRepository.save(user));
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email or password is incorrect"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is inactive");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Email or password is incorrect");
        }

        AuthToken token = AuthToken.builder()
                .token(UUID.randomUUID().toString().replace("-", ""))
                .user(user)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(securityProperties.token().expiryDays()))
                .lastUsedAt(LocalDateTime.now())
                .build();

        authTokenRepository.save(token);
        return new AuthDtos.AuthResponse(token.getToken(), toProfile(user));
    }

    @Transactional(readOnly = true)
    public AuthDtos.MeResponse me() {
        User user = currentUserService.requireCurrentUser();
        return new AuthDtos.MeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private User buildUser(String fullName, String email, String phone, String rawPassword,
            com.salonnbooking.domain.Gender gender, Role role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .gender(gender)
                .role(role)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private AccountDtos.ProfileResponse toProfile(User user) {
        return new AccountDtos.ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
