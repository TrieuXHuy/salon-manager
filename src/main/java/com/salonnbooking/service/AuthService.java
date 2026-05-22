package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.salonnbooking.api.dto.AuthRequests;
import com.salonnbooking.domain.User;
import com.salonnbooking.domain.UserRole;
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
		user.setRole(UserRole.CUSTOMER);
		return userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public User login(AuthRequests.Login req) {
		String username = req.username().trim();
		return userRepository.findByUsername(username)
				.filter(user -> user.getPassword().equals(req.password()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
	}

	@Transactional(readOnly = true)
	public List<User> findAllUsers(String requesterUsername) {
		requireOwner(requesterUsername);
		return userRepository.findAll();
	}

	public User changeRole(Integer userId, AuthRequests.ChangeRole req) {
		requireOwner(req.requesterUsername());
		if (req.role() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
		}
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		user.setRole(req.role());
		return userRepository.save(user);
	}

	private void requireOwner(String username) {
		User requester = userRepository.findByUsername(username.trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester not found"));
		if (requester.getRole() != UserRole.OWNER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can manage users");
		}
	}
}
