package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.StaffService;
import com.salonnbooking.domain.StaffWorkingHour;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.AppointmentServiceRepository;
import com.salonnbooking.repository.PaymentRepository;
import com.salonnbooking.repository.ServiceRepository;
import com.salonnbooking.repository.StaffServiceRepository;
import com.salonnbooking.repository.StaffWorkingHourRepository;
import com.salonnbooking.repository.UserRepository;
import com.salonnbooking.security.CurrentUserService;

@Service
public class BookingService {

    private static final int SLOT_STEP_MINUTES = 15;
    private static final Collection<AppointmentStatus> BLOCKING_STATUSES = List.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.IN_PROGRESS);

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final StaffServiceRepository staffServiceRepository;
    private final StaffWorkingHourRepository staffWorkingHourRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;

    public BookingService(
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            StaffServiceRepository staffServiceRepository,
            StaffWorkingHourRepository staffWorkingHourRepository,
            AppointmentRepository appointmentRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            PaymentRepository paymentRepository,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.staffServiceRepository = staffServiceRepository;
        this.staffWorkingHourRepository = staffWorkingHourRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
        this.paymentRepository = paymentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.StaffResponse> getStaffByService(Long serviceId) {
        com.salonnbooking.domain.Service service = getActiveService(serviceId);
        return staffServiceRepository.findByServiceId(service.getId()).stream()
                .map(StaffService::getStaff)
                .filter(staff -> staff != null && staff.getRole() == Role.STAFF)
                .filter(staff -> Boolean.TRUE.equals(staff.getIsActive()))
                .map(this::toStaffResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.AvailableSlotResponse> getAvailableSlots(Long staffId, LocalDate date, List<Long> serviceIds) {
        User staff = getActiveStaff(staffId);
        List<com.salonnbooking.domain.Service> services = getActiveServices(serviceIds);
        ensureStaffCanDoServices(staff.getId(), services);

        int totalDuration = services.stream()
                .map(com.salonnbooking.domain.Service::getDurationMinutes)
                .reduce(0, Integer::sum);
        if (totalDuration <= 0) {
            throw new IllegalArgumentException("Total duration must be greater than 0");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<StaffWorkingHour> workingHours = staffWorkingHourRepository.findByStaffId(staff.getId()).stream()
                .filter(hour -> hour.getDayOfWeek() == dayOfWeek)
                .filter(hour -> Boolean.TRUE.equals(hour.getIsActive()))
                .toList();

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Appointment> existingAppointments = appointmentRepository
                .findByStaffIdAndAppointmentStartLessThanAndAppointmentEndGreaterThanAndStatusIn(
                        staff.getId(),
                        dayEnd,
                        dayStart,
                        BLOCKING_STATUSES);

        return workingHours.stream()
                .flatMap(hour -> buildSlots(date, hour, totalDuration, existingAppointments).stream())
                .toList();
    }

    @Transactional
    public BookingDtos.AppointmentResponse createAppointment(BookingDtos.CreateAppointmentRequest request) {
        User customer = currentUserService.requireCurrentUser();
        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Only CUSTOMER can create appointments");
        }

        User staff = getActiveStaff(request.staffId());
        List<com.salonnbooking.domain.Service> services = getActiveServices(request.serviceIds());
        ensureStaffCanDoServices(staff.getId(), services);

        int totalDuration = services.stream()
                .map(com.salonnbooking.domain.Service::getDurationMinutes)
                .reduce(0, Integer::sum);
        LocalDateTime appointmentEnd = request.appointmentStart().plusMinutes(totalDuration);
        ensureSlotAvailable(staff.getId(), request.appointmentStart(), appointmentEnd, services);

        BigDecimal totalAmount = services.stream()
                .map(com.salonnbooking.domain.Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = Appointment.builder()
                .customer(customer)
                .staff(staff)
                .appointmentStart(request.appointmentStart())
                .appointmentEnd(appointmentEnd)
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
                .paymentStatus(PaymentStatus.UNPAID)
                .createdAt(now)
                .build());

        return toAppointmentResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<BookingDtos.AppointmentResponse> getMyAppointments(AppointmentStatus status) {
        User customer = currentUserService.requireCurrentUser();
        List<Appointment> appointments = status == null
                ? appointmentRepository.findByCustomerIdOrderByAppointmentStartDesc(customer.getId())
                : appointmentRepository.findByCustomerIdAndStatusOrderByAppointmentStartDesc(customer.getId(), status);
        return appointments.stream().map(this::toAppointmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingDtos.AppointmentResponse getMyAppointmentDetail(Long id) {
        User customer = currentUserService.requireCurrentUser();
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        if (appointment.getCustomer() == null || !appointment.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Appointment not found with id: " + id);
        }
        return toAppointmentResponse(appointment);
    }

    @Transactional
    public BookingDtos.AppointmentResponse cancelMyAppointment(Long id, BookingDtos.CancelAppointmentRequest request) {
        User customer = currentUserService.requireCurrentUser();
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        if (appointment.getCustomer() == null || !appointment.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Appointment not found with id: " + id);
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only PENDING or CONFIRMED appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(request.cancelReason());
        appointment.setUpdatedAt(LocalDateTime.now());
        return toAppointmentResponse(appointmentRepository.save(appointment));
    }

    private void ensureSlotAvailable(
            Long staffId,
            LocalDateTime appointmentStart,
            LocalDateTime appointmentEnd,
            List<com.salonnbooking.domain.Service> services) {
        List<BookingDtos.AvailableSlotResponse> slots = getAvailableSlots(
                staffId,
                appointmentStart.toLocalDate(),
                services.stream().map(com.salonnbooking.domain.Service::getId).toList());
        boolean available = slots.stream().anyMatch(slot -> slot.start().equals(appointmentStart)
                && slot.end().equals(appointmentEnd));
        if (!available) {
            throw new IllegalArgumentException("Selected slot is not available");
        }
    }

    private List<BookingDtos.AvailableSlotResponse> buildSlots(
            LocalDate date,
            StaffWorkingHour hour,
            int totalDuration,
            List<Appointment> existingAppointments) {
        LocalDateTime cursor = LocalDateTime.of(date, hour.getStartTime());
        LocalDateTime workEnd = LocalDateTime.of(date, hour.getEndTime());

        return java.util.stream.Stream.iterate(cursor, slotStart -> !slotStart.plusMinutes(totalDuration).isAfter(workEnd),
                        slotStart -> slotStart.plusMinutes(SLOT_STEP_MINUTES))
                .filter(slotStart -> !overlapsExisting(slotStart, slotStart.plusMinutes(totalDuration), existingAppointments))
                .map(slotStart -> new BookingDtos.AvailableSlotResponse(slotStart, slotStart.plusMinutes(totalDuration)))
                .toList();
    }

    private boolean overlapsExisting(LocalDateTime start, LocalDateTime end, List<Appointment> existingAppointments) {
        return existingAppointments.stream()
                .anyMatch(appointment -> start.isBefore(appointment.getAppointmentEnd())
                        && end.isAfter(appointment.getAppointmentStart()));
    }

    private User getActiveStaff(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        if (staff.getRole() != Role.STAFF || !Boolean.TRUE.equals(staff.getIsActive())) {
            throw new ResourceNotFoundException("Staff not found with id: " + staffId);
        }
        return staff;
    }

    private com.salonnbooking.domain.Service getActiveService(Long serviceId) {
        com.salonnbooking.domain.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
        if (!Boolean.TRUE.equals(service.getIsActive())) {
            throw new ResourceNotFoundException("Service not found with id: " + serviceId);
        }
        return service;
    }

    private List<com.salonnbooking.domain.Service> getActiveServices(List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new IllegalArgumentException("serviceIds is required");
        }
        List<com.salonnbooking.domain.Service> services = serviceRepository.findAllById(serviceIds);
        if (services.size() != Set.copyOf(serviceIds).size()) {
            throw new ResourceNotFoundException("One or more services were not found");
        }
        services.forEach(service -> {
            if (!Boolean.TRUE.equals(service.getIsActive())) {
                throw new ResourceNotFoundException("Service not found with id: " + service.getId());
            }
        });
        return services;
    }

    private void ensureStaffCanDoServices(Long staffId, List<com.salonnbooking.domain.Service> services) {
        Set<Long> staffServiceIds = staffServiceRepository.findByStaffId(staffId).stream()
                .map(StaffService::getService)
                .filter(service -> service != null)
                .map(com.salonnbooking.domain.Service::getId)
                .collect(java.util.stream.Collectors.toSet());

        boolean canDoAll = services.stream().allMatch(service -> staffServiceIds.contains(service.getId()));
        if (!canDoAll) {
            throw new IllegalArgumentException("Staff cannot perform one or more selected services");
        }
    }

    private BookingDtos.StaffResponse toStaffResponse(User staff) {
        return new BookingDtos.StaffResponse(
                staff.getId(),
                staff.getFullName(),
                staff.getEmail(),
                staff.getPhone(),
                staff.getGender(),
                staff.getIsActive());
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
