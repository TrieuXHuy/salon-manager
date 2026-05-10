package com.salonnbooking.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salonnbooking.domain.UserAccount;
import com.salonnbooking.repository.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {
	private final UserAccountRepository userAccountRepository;

	public AuthService(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	public UserAccount authenticate(String username, String password) {
		UserAccount user = userAccountRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		if (!Boolean.TRUE.equals(user.getIsActive())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is disabled");
		}
		if (!user.getPassword().equals(password)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		return user;
	}
}
