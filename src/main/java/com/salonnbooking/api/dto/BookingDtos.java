package com.salonnbooking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStatus;

public final class BookingDtos {

    private BookingDtos() {
    }

    public record StaffResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            Gender gender,
            Boolean isActive) {
    }

    public record AvailableSlotResponse(
            LocalDateTime start,
            LocalDateTime end) {
    }

    public record CreateAppointmentRequest(
            Long staffId,
            LocalDateTime appointmentStart,
            List<Long> serviceIds,
            String note) {
    }

    public record CancelAppointmentRequest(String cancelReason) {
    }

    public record AppointmentServiceResponse(
            Long serviceId,
            String serviceName,
            BigDecimal priceSnapshot,
            Integer durationSnapshot) {
    }

    public record PaymentResponse(
            Long id,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            LocalDateTime paidAt,
            LocalDateTime createdAt) {
    }

    public record UpdatePaymentRequest(PaymentMethod paymentMethod) {
    }

    public record AppointmentResponse(
            Long id,
            Long customerId,
            String customerName,
            Long staffId,
            String staffName,
            LocalDateTime appointmentStart,
            LocalDateTime appointmentEnd,
            AppointmentStatus status,
            String note,
            String cancelReason,
            BigDecimal totalAmount,
            List<AppointmentServiceResponse> services,
            List<PaymentResponse> payments,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }
}
