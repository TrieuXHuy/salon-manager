package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.service.AdminUserManagementService;

@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminStaffController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping
    public List<AdminUserDtos.UserResponse> findAll() {
        return adminUserManagementService.getStaffs();
    }

    @PostMapping
    public AdminUserDtos.UserResponse create(@RequestBody AdminUserDtos.CreateStaffRequest request) {
        return adminUserManagementService.createStaff(request);
    }

    @PutMapping("/{id}")
    public AdminUserDtos.UserResponse update(@PathVariable Long id, @RequestBody AdminUserDtos.UpdateUserRequest request) {
        return adminUserManagementService.updateStaff(id, request);
    }

    @PatchMapping("/{id}/toggle-active")
    public AdminUserDtos.UserResponse toggleActive(@PathVariable Long id) {
        return adminUserManagementService.toggleStaffActive(id);
    }
}
