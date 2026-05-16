package com.salonnbooking.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.AccountDtos;
import com.salonnbooking.api.dto.AuthDtos;
import com.salonnbooking.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AccountDtos.ProfileResponse register(@RequestBody AuthDtos.RegisterRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/register-admin")
    public AccountDtos.ProfileResponse registerAdmin(@RequestBody AuthDtos.RegisterAdminRequest request) {
        return authService.registerAdmin(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthDtos.MeResponse me() {
        return authService.me();
    }
}
