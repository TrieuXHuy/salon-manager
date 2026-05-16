package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AdminStaffServiceDtos;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.ServiceCategory;
import com.salonnbooking.domain.StaffService;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.ServiceRepository;
import com.salonnbooking.repository.StaffServiceRepository;
import com.salonnbooking.repository.UserRepository;

@Service
public class AdminStaffCapabilityService {

    private final StaffServiceRepository staffServiceRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public AdminStaffCapabilityService(
            StaffServiceRepository staffServiceRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository) {
        this.staffServiceRepository = staffServiceRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminStaffServiceDtos.StaffServiceResponse> getServicesByStaff(Long staffId) {
        validateStaff(staffId);
        return staffServiceRepository.findByStaffId(staffId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminStaffServiceDtos.StaffServiceResponse assignServiceToStaff(Long staffId, Long serviceId) {
        User staff = validateStaff(staffId);
        com.salonnbooking.domain.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        return staffServiceRepository.findByStaffIdAndServiceId(staffId, serviceId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    StaffService link = StaffService.builder().staff(staff).service(service).build();
                    return toResponse(staffServiceRepository.save(link));
                });
    }

    @Transactional
    public void removeServiceFromStaff(Long staffId, Long serviceId) {
        validateStaff(staffId);
        StaffService link = staffServiceRepository.findByStaffIdAndServiceId(staffId, serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("StaffService not found"));
        staffServiceRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public List<AdminStaffServiceDtos.StaffServiceResponse> getStaffByService(Long serviceId) {
        com.salonnbooking.domain.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
        ServiceCategory category = service.getCategory();
        if (category != null) {
            category.getName();
        }
        return staffServiceRepository.findByServiceId(serviceId).stream().map(this::toResponse).toList();
    }

    private User validateStaff(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        if (staff.getRole() != Role.STAFF) {
            throw new ResourceNotFoundException("Staff not found with id: " + staffId);
        }
        return staff;
    }

    private AdminStaffServiceDtos.StaffServiceResponse toResponse(StaffService link) {
        return new AdminStaffServiceDtos.StaffServiceResponse(
                link.getId(),
                link.getStaff() != null ? link.getStaff().getId() : null,
                link.getStaff() != null ? link.getStaff().getFullName() : null,
                link.getService() != null ? link.getService().getId() : null,
                link.getService() != null ? link.getService().getName() : null,
                link.getStaff() != null ? link.getStaff().getIsActive() : null,
                link.getService() != null ? link.getService().getIsActive() : null);
    }
}
