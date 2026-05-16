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

import com.salonnbooking.api.dto.AdminWorkingHourDtos;
import com.salonnbooking.service.AdminStaffWorkingHourService;

@RestController
@RequestMapping("/api/admin")
public class AdminStaffWorkingHourController {

    private final AdminStaffWorkingHourService adminStaffWorkingHourService;

    public AdminStaffWorkingHourController(AdminStaffWorkingHourService adminStaffWorkingHourService) {
        this.adminStaffWorkingHourService = adminStaffWorkingHourService;
    }

    @GetMapping("/staff/{staffId}/working-hours")
    public List<AdminWorkingHourDtos.WorkingHourResponse> getByStaff(@PathVariable Long staffId) {
        return adminStaffWorkingHourService.getByStaff(staffId);
    }

    @PostMapping("/staff/{staffId}/working-hours")
    public AdminWorkingHourDtos.WorkingHourResponse create(
            @PathVariable Long staffId,
            @RequestBody AdminWorkingHourDtos.CreateWorkingHourRequest request) {
        return adminStaffWorkingHourService.createForStaff(staffId, request);
    }

    @PutMapping("/staff-working-hours/{id}")
    public AdminWorkingHourDtos.WorkingHourResponse update(
            @PathVariable Long id,
            @RequestBody AdminWorkingHourDtos.UpdateWorkingHourRequest request) {
        return adminStaffWorkingHourService.update(id, request);
    }

    @PatchMapping("/staff-working-hours/{id}/toggle-active")
    public AdminWorkingHourDtos.WorkingHourResponse toggle(@PathVariable Long id) {
        return adminStaffWorkingHourService.toggleActive(id);
    }
}
