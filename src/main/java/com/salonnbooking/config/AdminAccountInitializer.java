package com.salonnbooking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.salonnbooking.domain.User;
import com.salonnbooking.domain.UserRole;
import com.salonnbooking.repository.UserRepository;

@Component
public class AdminAccountInitializer implements CommandLineRunner {
	private final UserRepository userRepository;

	public AdminAccountInitializer(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void run(String... args) {
		userRepository.findByUsername("admin").ifPresent(existingAdmin -> {
			existingAdmin.setRole(UserRole.OWNER);
			userRepository.save(existingAdmin);
		});
		if (userRepository.existsByUsername("admin")) {
			return;
		}

		User admin = new User();
		admin.setUsername("admin");
		admin.setPassword("123456");
		admin.setRole(UserRole.OWNER);
		userRepository.save(admin);
	}
}
