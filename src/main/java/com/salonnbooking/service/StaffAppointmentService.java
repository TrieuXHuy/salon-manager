package com.salonnbooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.api.dto.StaffDtos;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.AppointmentServiceRepository;
import com.salonnbooking.security.CurrentUserService;

@Service
public class StaffAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final CurrentUserService currentUserService;

    public StaffAppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            CurrentUserService currentUserService) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.AppointmentResponse> getMyAppointments(LocalDate date, AppointmentStatus status) {
        User staff = requireStaff();
        List<Appointment> appointments;
        if (date != null && status != null) {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.plusDays(1).atStartOfDay();
            appointments = appointmentRepository.findByStaffIdAndAppointmentStartBetweenAndStatusOrderByAppointmentStartDesc(
                    staff.getId(), from, to, status);
        } else if (date != null) {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.plusDays(1).atStartOfDay();
            appointments = appointmentRepository.findByStaffIdAndAppointmentStartBetweenOrderByAppointmentStartDesc(
                    staff.getId(), from, to);
        } else if (status != null) {
            appointments = appointmentRepository.findByStaffIdAndStatusOrderByAppointmentStartDesc(staff.getId(), status);
        } else {
            appointments = appointmentRepository.findByStaffIdOrderByAppointmentStartDesc(staff.getId());
        }

        return appointments.stream().map(this::toAppointmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingDtos.AppointmentResponse getMyAppointmentDetail(Long id) {
        User staff = requireStaff();
        Appointment appointment = findAssignedAppointment(staff.getId(), id);
        return toAppointmentResponse(appointment);
    }

    @Transactional
    public BookingDtos.AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        User staff = requireStaff();
        Appointment appointment = findAssignedAppointment(staff.getId(), id);
        validateTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);
        appointment.setUpdatedAt(LocalDateTime.now());
        return toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public StaffDtos.CustomerProfileResponse getCustomerProfile(Long customerId) {
        User staff = requireStaff();
        List<Appointment> assigned = appointmentRepository.findByStaffIdOrderByAppointmentStartDesc(staff.getId()).stream()
                .filter(appointment -> appointment.getCustomer() != null && appointment.getCustomer().getId().equals(customerId))
                .toList();
        if (assigned.isEmpty()) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        User customer = assigned.get(0).getCustomer();
        return new StaffDtos.CustomerProfileResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getGender(),
                customer.getIsActive(),
                customer.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<StaffDtos.CustomerHistoryItemResponse> getCustomerHistory(Long customerId) {
        User staff = requireStaff();
        return appointmentRepository.findByStaffIdOrderByAppointmentStartDesc(staff.getId()).stream()
                .filter(appointment -> appointment.getCustomer() != null && appointment.getCustomer().getId().equals(customerId))
                .map(this::toHistoryItem)
                .toList();
    }

    private Appointment findAssignedAppointment(Long staffId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        if (appointment.getStaff() == null || !appointment.getStaff().getId().equals(staffId)) {
            throw new ResourceNotFoundException("Appointment not found with id: " + appointmentId);
        }
        return appointment;
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus target) {
        boolean valid = (current == AppointmentStatus.PENDING && target == AppointmentStatus.CONFIRMED)
                || (current == AppointmentStatus.CONFIRMED && target == AppointmentStatus.IN_PROGRESS)
                || (current == AppointmentStatus.IN_PROGRESS && target == AppointmentStatus.COMPLETED);
        if (!valid) {
            throw new IllegalArgumentException("Invalid status transition: " + current + " -> " + target);
        }
    }

    private User requireStaff() {
        User user = currentUserService.requireCurrentUser();
        if (user.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Only STAFF can access this resource");
        }
        return user;
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
                List.of(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }

    private StaffDtos.CustomerHistoryItemResponse toHistoryItem(Appointment appointment) {
        List<BookingDtos.AppointmentServiceResponse> services = appointmentServiceRepository
                .findByAppointmentId(appointment.getId()).stream()
                .map(item -> new BookingDtos.AppointmentServiceResponse(
                        item.getService() != null ? item.getService().getId() : null,
                        item.getService() != null ? item.getService().getName() : null,
                        item.getPriceSnapshot(),
                        item.getDurationSnapshot()))
                .toList();

        return new StaffDtos.CustomerHistoryItemResponse(
                appointment.getId(),
                appointment.getStaff() != null ? appointment.getStaff().getId() : null,
                appointment.getStaff() != null ? appointment.getStaff().getFullName() : null,
                appointment.getAppointmentStart(),
                appointment.getAppointmentEnd(),
                appointment.getStatus(),
                appointment.getNote(),
                services);
    }
}
