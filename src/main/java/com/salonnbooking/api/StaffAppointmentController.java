package com.salonnbooking.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.api.dto.StaffDtos;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.service.StaffAppointmentService;

@RestController
@RequestMapping("/api/staff")
public class StaffAppointmentController {

    private final StaffAppointmentService staffAppointmentService;

    public StaffAppointmentController(StaffAppointmentService staffAppointmentService) {
        this.staffAppointmentService = staffAppointmentService;
    }

    @GetMapping("/appointments")
    public List<BookingDtos.AppointmentResponse> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status) {
        return staffAppointmentService.getMyAppointments(date, status);
    }

    @GetMapping("/appointments/{id}")
    public BookingDtos.AppointmentResponse getAppointmentDetail(@PathVariable Long id) {
        return staffAppointmentService.getMyAppointmentDetail(id);
    }

    @PatchMapping("/appointments/{id}/status")
    public BookingDtos.AppointmentResponse updateStatus(
            @PathVariable Long id,
            @RequestBody StaffDtos.UpdateAppointmentStatusRequest request) {
        return staffAppointmentService.updateAppointmentStatus(id, request.status());
    }

    @GetMapping("/customers/{customerId}")
    public StaffDtos.CustomerProfileResponse getCustomer(@PathVariable Long customerId) {
        return staffAppointmentService.getCustomerProfile(customerId);
    }

    @GetMapping("/customers/{customerId}/history")
    public List<StaffDtos.CustomerHistoryItemResponse> getCustomerHistory(@PathVariable Long customerId) {
        return staffAppointmentService.getCustomerHistory(customerId);
    }
}
