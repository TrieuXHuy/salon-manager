package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/{id}")
    public AdminUserDtos.UserResponse update(@PathVariable Long id, @RequestBody AdminUserDtos.UpdateUserRequest request) {
        return adminUserManagementService.updateCustomer(id, request);
    }

    @PatchMapping("/{id}/toggle-active")
    public AdminUserDtos.UserResponse toggleActive(@PathVariable Long id) {
        return adminUserManagementService.toggleCustomerActive(id);
    }
}
