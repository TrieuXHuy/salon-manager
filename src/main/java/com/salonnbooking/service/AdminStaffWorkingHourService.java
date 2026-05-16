package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AdminWorkingHourDtos;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.StaffWorkingHour;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.StaffWorkingHourRepository;
import com.salonnbooking.repository.UserRepository;

@Service
public class AdminStaffWorkingHourService {

    private final StaffWorkingHourRepository staffWorkingHourRepository;
    private final UserRepository userRepository;

    public AdminStaffWorkingHourService(StaffWorkingHourRepository staffWorkingHourRepository, UserRepository userRepository) {
        this.staffWorkingHourRepository = staffWorkingHourRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminWorkingHourDtos.WorkingHourResponse> getByStaff(Long staffId) {
        validateStaff(staffId);
        return staffWorkingHourRepository.findByStaffId(staffId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminWorkingHourDtos.WorkingHourResponse createForStaff(Long staffId, AdminWorkingHourDtos.CreateWorkingHourRequest request) {
        User staff = validateStaff(staffId);
        StaffWorkingHour hour = StaffWorkingHour.builder()
                .staff(staff)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return toResponse(staffWorkingHourRepository.save(hour));
    }

    @Transactional
    public AdminWorkingHourDtos.WorkingHourResponse update(Long id, AdminWorkingHourDtos.UpdateWorkingHourRequest request) {
        StaffWorkingHour hour = staffWorkingHourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffWorkingHour not found with id: " + id));
        hour.setDayOfWeek(request.dayOfWeek());
        hour.setStartTime(request.startTime());
        hour.setEndTime(request.endTime());
        if (request.isActive() != null) {
            hour.setIsActive(request.isActive());
        }
        return toResponse(staffWorkingHourRepository.save(hour));
    }

    @Transactional
    public AdminWorkingHourDtos.WorkingHourResponse toggleActive(Long id) {
        StaffWorkingHour hour = staffWorkingHourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffWorkingHour not found with id: " + id));
        hour.setIsActive(!Boolean.TRUE.equals(hour.getIsActive()));
        return toResponse(staffWorkingHourRepository.save(hour));
    }

    private User validateStaff(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        if (staff.getRole() != Role.STAFF) {
            throw new ResourceNotFoundException("Staff not found with id: " + staffId);
        }
        return staff;
    }

    private AdminWorkingHourDtos.WorkingHourResponse toResponse(StaffWorkingHour hour) {
        return new AdminWorkingHourDtos.WorkingHourResponse(
                hour.getId(),
                hour.getStaff() != null ? hour.getStaff().getId() : null,
                hour.getStaff() != null ? hour.getStaff().getFullName() : null,
                hour.getDayOfWeek(),
                hour.getStartTime(),
                hour.getEndTime(),
                hour.getIsActive());
    }
}
