package com.salonnbooking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.salonnbooking.api.dto.AuthRequests;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.UserRepository;

@Service
@Transactional
public class AuthService {
	private final UserRepository userRepository;

	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User register(AuthRequests.Register req) {
		String username = req.username().trim();
		if (userRepository.existsByUsername(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}

		User user = new User();
		user.setUsername(username);
		user.setPassword(req.password());
		return userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public User login(AuthRequests.Login req) {
		String username = req.username().trim();
		return userRepository.findByUsername(username)
				.filter(user -> user.getPassword().equals(req.password()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
	}
}
