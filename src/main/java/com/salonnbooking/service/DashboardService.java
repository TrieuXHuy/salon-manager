package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.DashboardRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.PaymentRepository;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {
	private final CustomerRepository customerRepository;
	private final AppointmentRepository appointmentRepository;
	private final PaymentRepository paymentRepository;
	private final ServiceRepository serviceRepository;

	public DashboardService(
			CustomerRepository customerRepository,
			AppointmentRepository appointmentRepository,
			PaymentRepository paymentRepository,
			ServiceRepository serviceRepository) {
		this.customerRepository = customerRepository;
		this.appointmentRepository = appointmentRepository;
		this.paymentRepository = paymentRepository;
		this.serviceRepository = serviceRepository;
	}

	public DashboardRequests.DashboardResponse getDashboard() {
		int totalCustomers = (int) customerRepository.count();

		LocalDate today = LocalDate.now();
		List<Appointment> todayAppointments = appointmentRepository
				.findAppointmentsBetween(
						today.atStartOfDay(),
						today.atTime(23, 59, 59));

		int totalAppointmentsToday = todayAppointments.size();
		int pendingAppointments = (int) todayAppointments.stream()
				.filter(a -> a.getStatus() == AppointmentStatus.pending)
				.count();

		BigDecimal todayRevenue = BigDecimal.ZERO;
		for (Appointment apt : todayAppointments) {
			List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
			for (Payment payment : payments) {
				if (payment.getPaymentStatus() == PaymentStatus.paid) {
					todayRevenue = todayRevenue.add(payment.getAmount());
				}
			}
		}

		YearMonth currentMonth = YearMonth.now();
		LocalDate monthStart = currentMonth.atDay(1);
		LocalDate monthEnd = currentMonth.atEndOfMonth();

		List<Appointment> monthAppointments = appointmentRepository
				.findAppointmentsBetween(
						monthStart.atStartOfDay(),
						monthEnd.atTime(23, 59, 59));

		BigDecimal monthlyRevenue = BigDecimal.ZERO;
		for (Appointment apt : monthAppointments) {
			List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
			for (Payment payment : payments) {
				if (payment.getPaymentStatus() == PaymentStatus.paid) {
					monthlyRevenue = monthlyRevenue.add(payment.getAmount());
				}
			}
		}

		int completedAppointments = (int) monthAppointments.stream()
				.filter(a -> a.getStatus() == AppointmentStatus.completed)
				.count();

		double completionRate = monthAppointments.size() > 0
				? (double) completedAppointments / monthAppointments.size() * 100
				: 0;

		String topServiceName = "N/A";
		Integer topServiceId = null;
		if (!monthAppointments.isEmpty()) {
			topServiceId = monthAppointments.stream()
					.collect(java.util.stream.Collectors
							.groupingByConcurrent(
									a -> a.getService().getId(),
									java.util.stream.Collectors.counting()))
					.entrySet()
					.stream()
					.max(java.util.Map.Entry.comparingByValue())
					.map(java.util.Map.Entry::getKey)
					.orElse(null);

			if (topServiceId != null) {
				topServiceName = serviceRepository.findById(topServiceId)
						.map(s -> s.getName())
						.orElse("N/A");
			}
		}

		return new DashboardRequests.DashboardResponse(
				totalCustomers,
				totalAppointmentsToday,
				pendingAppointments,
				todayRevenue,
				monthlyRevenue,
				completionRate,
				topServiceId,
				topServiceName);
	}

	public DashboardRequests.QuickStatsResponse getQuickStats() {
		YearMonth currentMonth = YearMonth.now();
		LocalDate monthStart = currentMonth.atDay(1);
		LocalDate monthEnd = currentMonth.atEndOfMonth();

		List<Appointment> monthAppointments = appointmentRepository
				.findAppointmentsBetween(
						monthStart.atStartOfDay(),
						monthEnd.atTime(23, 59, 59));

		int newCustomersThisMonth = (int) customerRepository.findAll().stream()
				.filter(c -> {
					LocalDateTime createdAt = c.getCreatedAt();
					return createdAt.isAfter(monthStart.atStartOfDay())
							&& createdAt.isBefore(monthEnd.atTime(23, 59, 59));
				})
				.count();

		BigDecimal revenueThisMonth = BigDecimal.ZERO;
		for (Appointment apt : monthAppointments) {
			List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
			for (Payment payment : payments) {
				if (payment.getPaymentStatus() == PaymentStatus.paid) {
					revenueThisMonth = revenueThisMonth.add(payment.getAmount());
				}
			}
		}

		YearMonth previousMonth = currentMonth.minusMonths(1);
		LocalDate prevMonthStart = previousMonth.atDay(1);
		LocalDate prevMonthEnd = previousMonth.atEndOfMonth();

		BigDecimal prevMonthRevenue = BigDecimal.ZERO;
		List<Appointment> prevMonthAppointments = appointmentRepository
				.findAppointmentsBetween(
						prevMonthStart.atStartOfDay(),
						prevMonthEnd.atTime(23, 59, 59));

		for (Appointment apt : prevMonthAppointments) {
			List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
			for (Payment payment : payments) {
				if (payment.getPaymentStatus() == PaymentStatus.paid) {
					prevMonthRevenue = prevMonthRevenue.add(payment.getAmount());
				}
			}
		}

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
}
