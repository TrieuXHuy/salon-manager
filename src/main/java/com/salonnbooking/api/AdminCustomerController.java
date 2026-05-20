package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.service.AdminUserManagementService;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminCustomerController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping
    public List<AdminUserDtos.UserResponse> findAll() {
        return adminUserManagementService.getCustomers();
    }

    @PostMapping
    public AdminUserDtos.UserResponse create(@RequestBody AdminUserDtos.CreateCustomerRequest request) {
        return adminUserManagementService.createCustomer(request);
    }

    @PutMapping("/{id}")
    public AdminUserDtos.UserResponse update(@PathVariable Long id, @RequestBody AdminUserDtos.UpdateUserRequest request) {
        return adminUserManagementService.updateCustomer(id, request);
    }

    @PatchMapping("/{id}/toggle-active")
    public AdminUserDtos.UserResponse toggleActive(@PathVariable Long id) {
        return adminUserManagementService.toggleCustomerActive(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminUserManagementService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
