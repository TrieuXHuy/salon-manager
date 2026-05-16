package com.salonnbooking.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AccountDtos;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.AuthTokenRepository;
import com.salonnbooking.repository.UserRepository;
import com.salonnbooking.security.CurrentUserService;

@Service
public class AccountService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            CurrentUserService currentUserService,
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AccountDtos.ProfileResponse getProfile() {
        return toProfile(currentUserService.requireCurrentUser());
    }

    @Transactional
    public AccountDtos.ProfileResponse updateProfile(AccountDtos.UpdateProfileRequest request) {
        User user = currentUserService.requireCurrentUser();
        if (request.email() != null
                && !request.email().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setGender(request.gender());
        user.setUpdatedAt(LocalDateTime.now());
        return toProfile(userRepository.save(user));
    }

    @Transactional
    public void changePassword(AccountDtos.ChangePasswordRequest request) {
        User user = currentUserService.requireCurrentUser();
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new BadCredentialsException("New password is required");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        authTokenRepository.findByUserIdAndIsActiveTrue(user.getId()).forEach(token -> token.setIsActive(false));
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
