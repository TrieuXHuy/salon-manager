package com.salonnbooking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.UserRepository;

@Service
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserDtos.UserResponse> getStaffs() {
        return userRepository.findByRole(Role.STAFF).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminUserDtos.UserResponse createStaff(AdminUserDtos.CreateStaffRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .gender(request.gender())
                .role(Role.STAFF)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserDtos.UserResponse updateStaff(Long id, AdminUserDtos.UpdateUserRequest request) {
        User user = getUserByRole(id, Role.STAFF);
        applyUpdate(user, request);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserDtos.UserResponse toggleStaffActive(Long id) {
        User user = getUserByRole(id, Role.STAFF);
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AdminUserDtos.UserResponse> getCustomers() {
        return userRepository.findByRole(Role.CUSTOMER).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminUserDtos.UserResponse updateCustomer(Long id, AdminUserDtos.UpdateUserRequest request) {
        User user = getUserByRole(id, Role.CUSTOMER);
        applyUpdate(user, request);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserDtos.UserResponse toggleCustomerActive(Long id) {
        User user = getUserByRole(id, Role.CUSTOMER);
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    private void applyUpdate(User user, AdminUserDtos.UpdateUserRequest request) {
        if (request.email() != null
                && !request.email().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setGender(request.gender());
        if (request.isActive() != null) {
            user.setIsActive(request.isActive());
        }
        user.setUpdatedAt(LocalDateTime.now());
    }

    private User getUserByRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (user.getRole() != role) {
            throw new ResourceNotFoundException(role.name() + " not found with id: " + id);
        }
        return user;
    }

    private AdminUserDtos.UserResponse toResponse(User user) {
        return new AdminUserDtos.UserResponse(
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
