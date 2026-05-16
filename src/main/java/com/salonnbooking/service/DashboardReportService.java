package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.DashboardReportDtos;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentService;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.AppointmentServiceRepository;
import com.salonnbooking.repository.PaymentRepository;

@Service
public class DashboardReportService {

    private static final int DEFAULT_LIMIT = 10;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final PaymentRepository paymentRepository;

    public DashboardReportService(
            AppointmentRepository appointmentRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            PaymentRepository paymentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentServiceRepository = appointmentServiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardReportDtos.DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<Appointment> todayAppointments = appointmentRepository
                .findByAppointmentStartBetweenOrderByAppointmentStartDesc(start, end);

        BigDecimal todayRevenue = paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)
                .filter(payment -> payment.getPaidAt() != null)
                .filter(payment -> !payment.getPaidAt().isBefore(start) && payment.getPaidAt().isBefore(end))
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardReportDtos.DashboardSummaryResponse(
                todayRevenue,
                todayAppointments.size(),
                countStatus(todayAppointments, AppointmentStatus.PENDING),
                countStatus(todayAppointments, AppointmentStatus.CONFIRMED),
                countStatus(todayAppointments, AppointmentStatus.COMPLETED),
                countStatus(todayAppointments, AppointmentStatus.CANCELLED));
    }

    @Transactional(readOnly = true)
    public List<DashboardReportDtos.TopServiceResponse> getTopServices(Integer limit) {
        int resolvedLimit = resolveLimit(limit);
        return appointmentServiceRepository.findAll().stream()
                .filter(item -> item.getAppointment() != null)
                .filter(item -> item.getAppointment().getStatus() != AppointmentStatus.CANCELLED)
                .filter(item -> item.getService() != null)
                .collect(Collectors.groupingBy(item -> item.getService().getId()))
                .values()
                .stream()
                .map(items -> {
                    AppointmentService first = items.get(0);
                    BigDecimal revenue = items.stream()
                            .map(AppointmentService::getPriceSnapshot)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new DashboardReportDtos.TopServiceResponse(
                            first.getService().getId(),
                            first.getService().getName(),
                            items.size(),
                            revenue);
                })
                .sorted(Comparator.comparingLong(DashboardReportDtos.TopServiceResponse::bookingCount).reversed())
                .limit(resolvedLimit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardReportDtos.TopStaffResponse> getTopStaff(Integer limit) {
        int resolvedLimit = resolveLimit(limit);
        return appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELLED)
                .filter(appointment -> appointment.getStaff() != null)
                .collect(Collectors.groupingBy(appointment -> appointment.getStaff().getId()))
                .values()
                .stream()
                .map(appointments -> {
                    Appointment first = appointments.get(0);
                    User staff = first.getStaff();
                    BigDecimal revenue = appointments.stream()
                            .map(Appointment::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new DashboardReportDtos.TopStaffResponse(
                            staff.getId(),
                            staff.getFullName(),
                            appointments.size(),
                            revenue);
                })
                .sorted(Comparator.comparingLong(DashboardReportDtos.TopStaffResponse::appointmentCount).reversed())
                .limit(resolvedLimit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardReportDtos.DailyRevenueResponse> getRevenueDaily(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate resolvedStart = startDate != null ? startDate : resolvedEnd.minusDays(29);
        if (resolvedStart.isAfter(resolvedEnd)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        Map<LocalDate, List<Payment>> paymentsByDate = paidPayments().stream()
                .filter(payment -> !payment.getPaidAt().toLocalDate().isBefore(resolvedStart))
                .filter(payment -> !payment.getPaidAt().toLocalDate().isAfter(resolvedEnd))
                .collect(Collectors.groupingBy(payment -> payment.getPaidAt().toLocalDate()));

        return resolvedStart.datesUntil(resolvedEnd.plusDays(1))
                .map(date -> toDailyRevenue(date, paymentsByDate.getOrDefault(date, List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardReportDtos.MonthlyRevenueResponse> getRevenueMonthly(Integer year) {
        int resolvedYear = year != null ? year : Year.now().getValue();
        Map<YearMonth, List<Payment>> paymentsByMonth = paidPayments().stream()
                .filter(payment -> payment.getPaidAt().getYear() == resolvedYear)
                .collect(Collectors.groupingBy(payment -> YearMonth.from(payment.getPaidAt())));

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> YearMonth.of(resolvedYear, month))
                .map(month -> toMonthlyRevenue(month, paymentsByMonth.getOrDefault(month, List.of())))
                .toList();
    }

    private long countStatus(List<Appointment> appointments, AppointmentStatus status) {
        return appointments.stream()
                .filter(appointment -> appointment.getStatus() == status)
                .count();
    }

    private List<Payment> paidPayments() {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)
                .filter(payment -> payment.getPaidAt() != null)
                .toList();
    }

    private DashboardReportDtos.DailyRevenueResponse toDailyRevenue(LocalDate date, List<Payment> payments) {
        BigDecimal revenue = sumPayments(payments);
        return new DashboardReportDtos.DailyRevenueResponse(date, revenue, payments.size());
    }

    private DashboardReportDtos.MonthlyRevenueResponse toMonthlyRevenue(YearMonth month, List<Payment> payments) {
        BigDecimal revenue = sumPayments(payments);
        return new DashboardReportDtos.MonthlyRevenueResponse(month, revenue, payments.size());
    }

    private BigDecimal sumPayments(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, 100);
    }
}
