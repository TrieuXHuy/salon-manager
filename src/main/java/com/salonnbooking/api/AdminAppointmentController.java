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
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.service.AdminAppointmentService;

@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    private final AdminAppointmentService adminAppointmentService;

    public AdminAppointmentController(AdminAppointmentService adminAppointmentService) {
        this.adminAppointmentService = adminAppointmentService;
    }

    @GetMapping
    public List<BookingDtos.AppointmentResponse> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) AppointmentStatus status) {
        return adminAppointmentService.getAppointments(date, staffId, customerId, status);
    }

    @GetMapping("/{id}")
    public BookingDtos.AppointmentResponse getAppointmentDetail(@PathVariable Long id) {
        return adminAppointmentService.getAppointmentDetail(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingDtos.AppointmentResponse cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) BookingDtos.CancelAppointmentRequest request) {
        return adminAppointmentService.cancelAppointment(id, request);
    }

    @PatchMapping("/{id}/payment")
    public BookingDtos.AppointmentResponse payAppointment(
            @PathVariable Long id,
            @RequestBody BookingDtos.UpdatePaymentRequest request) {
        return adminAppointmentService.payAppointment(id, request);
    }
}
