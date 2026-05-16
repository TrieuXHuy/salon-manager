package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.PaymentRepository;
import com.salonnbooking.security.CurrentUserService;

@Service
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserService currentUserService;

    public PaymentQueryService(
            PaymentRepository paymentRepository,
            AppointmentRepository appointmentRepository,
            CurrentUserService currentUserService) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.PaymentResponse> getMyAppointmentPayments(Long appointmentId) {
        User customer = currentUserService.requireCurrentUser();
        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Only CUSTOMER can access this resource");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        if (appointment.getCustomer() == null || !appointment.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Appointment not found with id: " + appointmentId);
        }

        return paymentRepository.findByAppointmentId(appointmentId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    private BookingDtos.PaymentResponse toPaymentResponse(com.salonnbooking.domain.Payment payment) {
        return new BookingDtos.PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getPaidAt(),
                payment.getCreatedAt());
    }
}
