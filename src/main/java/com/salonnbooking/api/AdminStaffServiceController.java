package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.AdminStaffServiceDtos;
import com.salonnbooking.service.AdminStaffCapabilityService;

@RestController
@RequestMapping("/api/admin")
public class AdminStaffServiceController {

    private final AdminStaffCapabilityService adminStaffCapabilityService;

    public AdminStaffServiceController(AdminStaffCapabilityService adminStaffCapabilityService) {
        this.adminStaffCapabilityService = adminStaffCapabilityService;
    }

    @GetMapping("/staff/{staffId}/services")
    public List<AdminStaffServiceDtos.StaffServiceResponse> getServicesByStaff(@PathVariable Long staffId) {
        return adminStaffCapabilityService.getServicesByStaff(staffId);
    }

    @PostMapping("/staff/{staffId}/services/{serviceId}")
    public AdminStaffServiceDtos.StaffServiceResponse assignService(
            @PathVariable Long staffId,
            @PathVariable Long serviceId) {
        return adminStaffCapabilityService.assignServiceToStaff(staffId, serviceId);
    }

    @DeleteMapping("/staff/{staffId}/services/{serviceId}")
    public ResponseEntity<Void> removeService(@PathVariable Long staffId, @PathVariable Long serviceId) {
        adminStaffCapabilityService.removeServiceFromStaff(staffId, serviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services/{serviceId}/staff")
    public List<AdminStaffServiceDtos.StaffServiceResponse> getStaffByService(@PathVariable Long serviceId) {
        return adminStaffCapabilityService.getStaffByService(serviceId);
    }
}
