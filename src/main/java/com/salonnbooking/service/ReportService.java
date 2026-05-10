package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ReportRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.PaymentRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {
	private final AppointmentRepository appointmentRepository;
	private final PaymentRepository paymentRepository;

	public ReportService(AppointmentRepository appointmentRepository, PaymentRepository paymentRepository) {
		this.appointmentRepository = appointmentRepository;
		this.paymentRepository = paymentRepository;
	}

	public List<ReportRequests.DailyRevenueResponse> getDailyRevenueReport(LocalDate startDate, LocalDate endDate) {
		List<Appointment> appointments = appointmentRepository
				.findAppointmentsBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
		Map<LocalDate, List<Appointment>> groupedByDate = appointments.stream()
				.collect(Collectors.groupingBy(a -> a.getAppointmentTime().toLocalDate()));

		List<ReportRequests.DailyRevenueResponse> reports = new ArrayList<>();
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			List<Appointment> dayAppointments = groupedByDate.getOrDefault(date, List.of());
			int completedCount = (int) dayAppointments.stream()
					.filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
					.count();
			BigDecimal totalRevenue = calculateRevenue(dayAppointments);
			reports.add(new ReportRequests.DailyRevenueResponse(date, totalRevenue, dayAppointments.size(), completedCount));
		}
		return reports;
	}

	public List<ReportRequests.ServiceRevenueResponse> getServiceRevenueReport() {
		List<Appointment> allAppointments = appointmentRepository.findAll();
		Map<Integer, List<Appointment>> groupedByService = allAppointments.stream()
				.flatMap(a -> a.getAppointmentServices().stream().map(item -> Map.entry(item.getService().getId(), a)))
				.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

		List<ReportRequests.ServiceRevenueResponse> reports = new ArrayList<>();
		for (Map.Entry<Integer, List<Appointment>> entry : groupedByService.entrySet()) {
			List<Appointment> serviceAppointments = entry.getValue();
			int appointmentCount = serviceAppointments.size();
			BigDecimal totalRevenue = calculateRevenue(serviceAppointments);
			BigDecimal avgRevenue = appointmentCount > 0
					? totalRevenue.divide(BigDecimal.valueOf(appointmentCount), 2, java.math.RoundingMode.HALF_UP)
					: BigDecimal.ZERO;
			String serviceName = serviceAppointments.stream()
					.flatMap(a -> a.getAppointmentServices().stream())
					.filter(item -> item.getService().getId().equals(entry.getKey()))
					.map(item -> item.getServiceNameSnapshot())
					.findFirst()
					.orElse("Unknown");
			reports.add(new ReportRequests.ServiceRevenueResponse(entry.getKey(), serviceName, appointmentCount,
					totalRevenue, avgRevenue));
		}

		return reports.stream()
				.sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
				.toList();
	}

	public List<ReportRequests.PaymentMethodResponse> getPaymentMethodReport() {
		List<Payment> paidPayments = paymentRepository.findAll().stream()
				.filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
				.toList();

		Map<PaymentMethod, List<Payment>> groupedByMethod = paidPayments.stream()
				.collect(Collectors.groupingBy(Payment::getPaymentMethod));

		BigDecimal totalAmount = paidPayments.stream()
				.map(Payment::getFinalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<ReportRequests.PaymentMethodResponse> reports = new ArrayList<>();
		for (Map.Entry<PaymentMethod, List<Payment>> entry : groupedByMethod.entrySet()) {
			BigDecimal methodTotal = entry.getValue().stream()
					.map(Payment::getFinalAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
					? methodTotal.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
							.multiply(BigDecimal.valueOf(100))
					: BigDecimal.ZERO;

			reports.add(new ReportRequests.PaymentMethodResponse(entry.getKey().name(), entry.getValue().size(),
					methodTotal, percentage));
		}
		return reports;
	}

	public ReportRequests.AppointmentStatsResponse getAppointmentStats() {
		List<Appointment> allAppointments = appointmentRepository.findAll();
		return new ReportRequests.AppointmentStatsResponse(
				allAppointments.size(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count());
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
