package com.salonnbooking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.UserAccount;
import com.salonnbooking.repository.UserAccountRepository;

@Component
public class DefaultAdminInitializer implements CommandLineRunner {
	private final UserAccountRepository userAccountRepository;

	public DefaultAdminInitializer(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	@Override
	public void run(String... args) {
		if (userAccountRepository.findByUsername("admin").isPresent()) {
			return;
		}

		UserAccount admin = new UserAccount();
		admin.setUsername("admin");
		admin.setPassword("admin123");
		admin.setFullName("System Administrator");
		admin.setPhone("0900000000");
		admin.setEmail("admin@salon.local");
		admin.setRole(Role.ADMIN);
		admin.setIsActive(true);
		userAccountRepository.save(admin);
	}
}
