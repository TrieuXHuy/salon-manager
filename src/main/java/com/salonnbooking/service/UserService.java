package com.salonnbooking.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.salonnbooking.domain.User;
import com.salonnbooking.repository.UserRepository;

import jakarta.persistence.EntityManager;

@Service
public class UserService extends BaseCrudService<User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, EntityManager entityManager, PasswordEncoder passwordEncoder) {
        super(repository, entityManager, User.class);
        this.userRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(User entity) {
        prepareUser(entity, true);
        return super.create(entity);
    }

    @Override
    public User update(Long id, User entity) {
        prepareUser(entity, false);
        return super.update(id, entity);
    }

    private void prepareUser(User user, boolean creating) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (creating && user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        user.setUpdatedAt(now);
        if (creating && user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            boolean emailExists = userRepository.existsByEmail(user.getEmail());
            if (creating && emailExists) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
        }
    }
}
