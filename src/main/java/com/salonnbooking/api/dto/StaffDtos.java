package com.salonnbooking.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Gender;

public final class StaffDtos {

    private StaffDtos() {
    }

    public record UpdateAppointmentStatusRequest(AppointmentStatus status) {
    }

    public record CustomerProfileResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            Gender gender,
            Boolean isActive,
            LocalDateTime createdAt) {
    }

    public record CustomerHistoryItemResponse(
            Long appointmentId,
            Long staffId,
            String staffName,
            LocalDateTime appointmentStart,
            LocalDateTime appointmentEnd,
            AppointmentStatus status,
            String note,
            List<BookingDtos.AppointmentServiceResponse> services) {
    }

    public record StaffAppointmentFilter(
            LocalDate date,
            AppointmentStatus status) {
    }
}
