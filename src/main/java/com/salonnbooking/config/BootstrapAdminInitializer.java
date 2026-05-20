package com.salonnbooking.config;

import java.time.LocalDateTime;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.UserRepository;

@Configuration
public class BootstrapAdminInitializer {

    @Bean
    ApplicationRunner bootstrapAdminRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SecurityProperties securityProperties) {
        return args -> {
            SecurityProperties.BootstrapAdmin admin = securityProperties.bootstrapAdmin();
            if (admin == null || !admin.enabled()) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            User existingAdmin = userRepository.findByEmail(admin.email())
                    .filter(user -> user.getRole() == Role.ADMIN)
                    .orElse(null);
            if (existingAdmin != null) {
                existingAdmin.setFullName(admin.fullName());
                existingAdmin.setPhone(admin.phone());
                existingAdmin.setPassword(passwordEncoder.encode(admin.password()));
                existingAdmin.setIsActive(true);
                existingAdmin.setUpdatedAt(now);
                userRepository.save(existingAdmin);
                return;
            }

            User user = User.builder()
                    .fullName(admin.fullName())
                    .email(admin.email())
                    .phone(admin.phone())
                    .password(passwordEncoder.encode(admin.password()))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userRepository.save(user);
        };
    }
}
