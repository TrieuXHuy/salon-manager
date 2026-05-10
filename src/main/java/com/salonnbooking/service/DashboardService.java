package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.DashboardRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentServiceItem;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.PaymentRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {
	private final CustomerRepository customerRepository;
	private final AppointmentRepository appointmentRepository;
	private final PaymentRepository paymentRepository;

	public DashboardService(
			CustomerRepository customerRepository,
			AppointmentRepository appointmentRepository,
			PaymentRepository paymentRepository) {
		this.customerRepository = customerRepository;
		this.appointmentRepository = appointmentRepository;
		this.paymentRepository = paymentRepository;
	}

	public DashboardRequests.DashboardResponse getDashboard() {
		int totalCustomers = (int) customerRepository.count();

		LocalDate today = LocalDate.now();
		List<Appointment> todayAppointments = appointmentsBetween(today.atStartOfDay(), today.atTime(23, 59, 59));
		List<Appointment> monthAppointments = getCurrentMonthAppointments();

		int totalAppointmentsToday = todayAppointments.size();
		int pendingAppointments = (int) todayAppointments.stream()
				.filter(a -> a.getStatus() == AppointmentStatus.PENDING)
				.count();

		BigDecimal todayRevenue = calculateRevenue(todayAppointments);
		BigDecimal monthlyRevenue = calculateRevenue(monthAppointments);

		int completedAppointments = (int) monthAppointments.stream()
				.filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
				.count();
		double completionRate = monthAppointments.isEmpty()
				? 0
				: (double) completedAppointments / monthAppointments.size() * 100;

		String topServiceName = null;
		Integer topServiceId = null;
		Map<Integer, Long> serviceCounts = monthAppointments.stream()
				.flatMap(a -> a.getAppointmentServices().stream())
				.collect(Collectors.groupingBy(item -> item.getService().getId(), Collectors.counting()));
		Map.Entry<Integer, Long> topEntry = serviceCounts.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.orElse(null);
		if (topEntry != null) {
			Integer resolvedTopServiceId = topEntry.getKey();
			topServiceId = resolvedTopServiceId;
			topServiceName = monthAppointments.stream()
					.flatMap(a -> a.getAppointmentServices().stream())
					.filter(item -> item.getService().getId().equals(resolvedTopServiceId))
					.map(AppointmentServiceItem::getServiceNameSnapshot)
					.findFirst()
					.orElse("N/A");
		}

		return new DashboardRequests.DashboardResponse(
				totalCustomers,
				totalAppointmentsToday,
				pendingAppointments,
				todayRevenue,
				monthlyRevenue,
				completionRate,
				topServiceId,
				topServiceName == null ? "N/A" : topServiceName);
	}

	public DashboardRequests.QuickStatsResponse getQuickStats() {
		YearMonth currentMonth = YearMonth.now();
		LocalDate monthStart = currentMonth.atDay(1);
		LocalDate monthEnd = currentMonth.atEndOfMonth();
		List<Appointment> monthAppointments = appointmentsBetween(monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));

		int newCustomersThisMonth = (int) customerRepository.findAll().stream()
				.filter(c -> {
					LocalDateTime createdAt = c.getCreatedAt();
					return !createdAt.isBefore(monthStart.atStartOfDay())
							&& !createdAt.isAfter(monthEnd.atTime(23, 59, 59));
				})
				.count();

		BigDecimal revenueThisMonth = calculateRevenue(monthAppointments);

		YearMonth previousMonth = currentMonth.minusMonths(1);
		BigDecimal prevMonthRevenue = calculateRevenue(
				appointmentsBetween(previousMonth.atDay(1).atStartOfDay(), previousMonth.atEndOfMonth().atTime(23, 59, 59)));

		double growthRate = prevMonthRevenue.compareTo(BigDecimal.ZERO) > 0
				? revenueThisMonth.subtract(prevMonthRevenue)
						.divide(prevMonthRevenue, 4, java.math.RoundingMode.HALF_UP)
						.doubleValue() * 100
				: 0;

		return new DashboardRequests.QuickStatsResponse(
				newCustomersThisMonth,
				monthAppointments.size(),
				revenueThisMonth,
				growthRate);
	}

	private List<Appointment> getCurrentMonthAppointments() {
		YearMonth currentMonth = YearMonth.now();
		return appointmentsBetween(
				currentMonth.atDay(1).atStartOfDay(),
				currentMonth.atEndOfMonth().atTime(23, 59, 59));
	}

	private List<Appointment> appointmentsBetween(LocalDateTime start, LocalDateTime end) {
		return appointmentRepository.findAppointmentsBetween(start, end).stream()
				.sorted(Comparator.comparing(Appointment::getAppointmentTime))
				.toList();
	}

	private BigDecimal calculateRevenue(List<Appointment> appointments) {
		return appointments.stream()
				.map(Appointment::getId)
				.distinct()
				.map(paymentRepository::findByAppointmentId)
				.flatMap(List::stream)
				.filter(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)
				.map(Payment::getFinalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
