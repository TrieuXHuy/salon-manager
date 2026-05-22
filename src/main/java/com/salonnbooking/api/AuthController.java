package com.salonnbooking.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.AuthRequests;
import com.salonnbooking.domain.User;
import com.salonnbooking.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthRequests.Response register(@Valid @RequestBody AuthRequests.Register req) {
		User user = authService.register(req);
		return AuthRequests.Response.from(user, "Register successful");
	}

	@PostMapping("/login")
	public AuthRequests.Response login(@Valid @RequestBody AuthRequests.Login req) {
		User user = authService.login(req);
		return AuthRequests.Response.from(user, "Login successful");
	}

	@PostMapping("/logout")
	public AuthRequests.Message logout() {
		return new AuthRequests.Message("Logout successful");
	}
}
