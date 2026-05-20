package com.salonnbooking.service;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.AppointmentServiceRepository;
import com.salonnbooking.repository.PaymentRepository;
import com.salonnbooking.repository.ServiceRepository;
import com.salonnbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public AdminAppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
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
    public BookingDtos.AppointmentResponse createAppointment(BookingDtos.AdminCreateAppointmentRequest request) {
        if (request == null || request.staffId() == null || request.appointmentStart() == null
                || request.serviceIds() == null || request.serviceIds().isEmpty()) {
            throw new IllegalArgumentException("staffId, appointmentStart and serviceIds are required");
        }

        User customer = resolveCustomer(request.customerId());
        User staff = userRepository.findById(request.staffId())
                .filter(user -> user.getRole() == Role.STAFF)
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + request.staffId()));

        List<com.salonnbooking.domain.Service> services = request.serviceIds().stream()
                .map(serviceId -> serviceRepository.findById(serviceId)
                        .filter(service -> Boolean.TRUE.equals(service.getIsActive()))
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId)))
                .toList();

        int totalDuration = services.stream()
                .map(com.salonnbooking.domain.Service::getDurationMinutes)
                .reduce(0, Integer::sum);
        if (totalDuration <= 0) {
            throw new IllegalArgumentException("Total duration must be greater than 0");
        }

        BigDecimal totalAmount = services.stream()
                .map(com.salonnbooking.domain.Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = Appointment.builder()
                .customer(customer)
                .staff(staff)
                .appointmentStart(request.appointmentStart())
                .appointmentEnd(request.appointmentStart().plusMinutes(totalDuration))
                .status(AppointmentStatus.PENDING)
                .note(request.note())
                .totalAmount(totalAmount)
                .createdAt(now)
                .updatedAt(now)
                .build();
        appointment = appointmentRepository.save(appointment);

        for (com.salonnbooking.domain.Service service : services) {
            appointmentServiceRepository.save(com.salonnbooking.domain.AppointmentService.builder()
                    .appointment(appointment)
                    .service(service)
                    .priceSnapshot(service.getPrice())
                    .durationSnapshot(service.getDurationMinutes())
                    .build());
        }

        paymentRepository.save(Payment.builder()
                .appointment(appointment)
                .amount(totalAmount)
                .paymentMethod(null)
                .paymentStatus(PaymentStatus.UNPAID)
                .createdAt(now)
                .build());

        return toAppointmentResponse(appointment);
    }

    private User resolveCustomer(Long customerId) {
        if (customerId != null) {
            return userRepository.findById(customerId)
                    .filter(user -> user.getRole() == Role.CUSTOMER)
                    .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        }
        return userRepository.findByRole(Role.CUSTOMER).stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No active customer found"));
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

    @Transactional
    public BookingDtos.AppointmentResponse updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        appointment.setStatus(status);
        appointment.setUpdatedAt(LocalDateTime.now());
        return toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public BookingDtos.AppointmentResponse payAppointment(Long id, BookingDtos.UpdatePaymentRequest request) {
        if (request == null || request.paymentMethod() == null) {
            throw new IllegalArgumentException("paymentMethod is required");
        }

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot pay CANCELLED appointment");
        }

        List<Payment> payments = paymentRepository.findByAppointmentId(id);
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("Payment not found for appointment id: " + id);
        }

        LocalDateTime now = LocalDateTime.now();
        for (Payment payment : payments) {
            payment.setPaymentMethod(request.paymentMethod());
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(now);
        }
        paymentRepository.saveAll(payments);
        return toAppointmentResponse(appointment);
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
                        payment.getPaymentMethod(),
                        payment.getPaymentStatus(),
                        payment.getPaidAt(),
                        payment.getCreatedAt()))
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
