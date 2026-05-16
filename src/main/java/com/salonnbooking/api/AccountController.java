package com.salonnbooking.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.AccountDtos;
import com.salonnbooking.service.AccountService;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public AccountDtos.ProfileResponse getProfile() {
        return accountService.getProfile();
    }

    @PutMapping("/profile")
    public AccountDtos.ProfileResponse updateProfile(@RequestBody AccountDtos.UpdateProfileRequest request) {
        return accountService.updateProfile(request);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody AccountDtos.ChangePasswordRequest request) {
        accountService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
