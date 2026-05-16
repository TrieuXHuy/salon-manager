package com.salonnbooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.AppointmentServiceRepository;
import com.salonnbooking.repository.PaymentRepository;

@Service
public class AdminAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final PaymentRepository paymentRepository;

    public AdminAppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            PaymentRepository paymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.AppointmentResponse> getAppointments(
            LocalDate date,
            Long staffId,
            Long customerId,
            AppointmentStatus status) {
        List<Appointment> appointments;

        if (date != null) {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.plusDays(1).atStartOfDay();
            appointments = findByDateFilters(from, to, staffId, customerId, status);
        } else {
            appointments = appointmentRepository.findAll();
            if (staffId != null) {
                appointments = appointments.stream()
                        .filter(appointment -> appointment.getStaff() != null && staffId.equals(appointment.getStaff().getId()))
                        .toList();
            }
            if (customerId != null) {
                appointments = appointments.stream()
                        .filter(appointment -> appointment.getCustomer() != null && customerId.equals(appointment.getCustomer().getId()))
                        .toList();
            }
            if (status != null) {
                appointments = appointments.stream()
                        .filter(appointment -> status == appointment.getStatus())
                        .toList();
            }
            appointments = appointments.stream()
                    .sorted((a, b) -> b.getAppointmentStart().compareTo(a.getAppointmentStart()))
                    .toList();
        }

        return appointments.stream().map(this::toAppointmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingDtos.AppointmentResponse getAppointmentDetail(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return toAppointmentResponse(appointment);
    }

    @Transactional
    public BookingDtos.AppointmentResponse cancelAppointment(Long id, BookingDtos.CancelAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel COMPLETED appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Appointment is already CANCELLED");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(request != null ? request.cancelReason() : null);
        appointment.setUpdatedAt(LocalDateTime.now());
        return toAppointmentResponse(appointmentRepository.save(appointment));
    }

    private List<Appointment> findByDateFilters(
            LocalDateTime from,
            LocalDateTime to,
            Long staffId,
            Long customerId,
            AppointmentStatus status) {
        if (staffId != null && customerId != null && status != null) {
            return appointmentRepository.findByAppointmentStartBetweenAndStaffIdAndCustomerIdAndStatusOrderByAppointmentStartDesc(
                    from, to, staffId, customerId, status);
        }
        if (staffId != null && customerId != null) {
            return appointmentRepository.findByAppointmentStartBetweenAndStaffIdAndCustomerIdOrderByAppointmentStartDesc(
                    from, to, staffId, customerId);
        }
        if (staffId != null && status != null) {
            return appointmentRepository.findByAppointmentStartBetweenAndStaffIdAndStatusOrderByAppointmentStartDesc(
                    from, to, staffId, status);
        }
        if (customerId != null && status != null) {
            return appointmentRepository.findByAppointmentStartBetweenAndCustomerIdAndStatusOrderByAppointmentStartDesc(
                    from, to, customerId, status);
        }
        if (staffId != null) {
            return appointmentRepository.findByAppointmentStartBetweenAndStaffIdOrderByAppointmentStartDesc(
                    from, to, staffId);
        }
        if (customerId != null) {
            return appointmentRepository.findByAppointmentStartBetweenAndCustomerIdOrderByAppointmentStartDesc(
                    from, to, customerId);
        }
        if (status != null) {
            return appointmentRepository.findByAppointmentStartBetweenAndStatusOrderByAppointmentStartDesc(
                    from, to, status);
        }
        return appointmentRepository.findByAppointmentStartBetweenOrderByAppointmentStartDesc(from, to);
    }

    private BookingDtos.AppointmentResponse toAppointmentResponse(Appointment appointment) {
        List<BookingDtos.AppointmentServiceResponse> services = appointmentServiceRepository
                .findByAppointmentId(appointment.getId()).stream()
                .map(item -> new BookingDtos.AppointmentServiceResponse(
                        item.getService() != null ? item.getService().getId() : null,
                        item.getService() != null ? item.getService().getName() : null,
                        item.getPriceSnapshot(),
                        item.getDurationSnapshot()))
                .toList();

        List<BookingDtos.PaymentResponse> payments = paymentRepository.findByAppointmentId(appointment.getId()).stream()
                .map(payment -> new BookingDtos.PaymentResponse(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getPaymentStatus()))
                .toList();

        return new BookingDtos.AppointmentResponse(
                appointment.getId(),
                appointment.getCustomer() != null ? appointment.getCustomer().getId() : null,
                appointment.getCustomer() != null ? appointment.getCustomer().getFullName() : null,
                appointment.getStaff() != null ? appointment.getStaff().getId() : null,
                appointment.getStaff() != null ? appointment.getStaff().getFullName() : null,
                appointment.getAppointmentStart(),
                appointment.getAppointmentEnd(),
                appointment.getStatus(),
                appointment.getNote(),
                appointment.getCancelReason(),
                appointment.getTotalAmount(),
                services,
                payments,
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }
}
