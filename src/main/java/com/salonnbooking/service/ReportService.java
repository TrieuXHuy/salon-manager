package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
				.findAppointmentsBetween(
						startDate.atStartOfDay(),
						endDate.atTime(23, 59, 59));

		Map<LocalDate, List<Appointment>> groupedByDate = appointments.stream()
				.collect(Collectors.groupingBy(a -> a.getAppointmentTime().toLocalDate()));

		List<ReportRequests.DailyRevenueResponse> reports = new ArrayList<>();

		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			List<Appointment> dayAppointments = groupedByDate.getOrDefault(date, new ArrayList<>());
			int completedCount = (int) dayAppointments.stream()
					.filter(a -> a.getStatus() == AppointmentStatus.completed)
					.count();

			BigDecimal totalRevenue = BigDecimal.ZERO;
			for (Appointment apt : dayAppointments) {
				List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
				for (Payment payment : payments) {
					if (payment.getPaymentStatus() == PaymentStatus.paid) {
						totalRevenue = totalRevenue.add(payment.getAmount());
					}
				}
			}

			reports.add(new ReportRequests.DailyRevenueResponse(
					date,
					totalRevenue,
					dayAppointments.size(),
					completedCount));
		}

		return reports;
	}

	public List<ReportRequests.ServiceRevenueResponse> getServiceRevenueReport() {
		List<Appointment> allAppointments = appointmentRepository.findAll();

		Map<Integer, List<Appointment>> groupedByService = allAppointments.stream()
				.collect(Collectors.groupingBy(a -> a.getService().getId()));

		List<ReportRequests.ServiceRevenueResponse> reports = new ArrayList<>();

		for (Map.Entry<Integer, List<Appointment>> entry : groupedByService.entrySet()) {
			List<Appointment> serviceAppointments = entry.getValue();
			int appointmentCount = serviceAppointments.size();

			BigDecimal totalRevenue = BigDecimal.ZERO;
			for (Appointment apt : serviceAppointments) {
				List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
				for (Payment payment : payments) {
					if (payment.getPaymentStatus() == PaymentStatus.paid) {
						totalRevenue = totalRevenue.add(payment.getAmount());
					}
				}
			}

			BigDecimal avgRevenue = appointmentCount > 0
					? totalRevenue.divide(BigDecimal.valueOf(appointmentCount), 2, java.math.RoundingMode.HALF_UP)
					: BigDecimal.ZERO;

			reports.add(new ReportRequests.ServiceRevenueResponse(
					entry.getKey(),
					serviceAppointments.get(0).getService().getName(),
					appointmentCount,
					totalRevenue,
					avgRevenue));
		}

		return reports.stream()
				.sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
				.collect(Collectors.toList());
	}

	public List<ReportRequests.PaymentMethodResponse> getPaymentMethodReport() {
		List<Payment> allPayments = paymentRepository.findAll();

		Map<PaymentMethod, List<Payment>> groupedByMethod = allPayments.stream()
				.filter(p -> p.getPaymentStatus() == PaymentStatus.paid)
				.collect(Collectors.groupingBy(Payment::getPaymentMethod));

		int totalPayments = allPayments.stream()
				.filter(p -> p.getPaymentStatus() == PaymentStatus.paid)
				.toList()
				.size();

		BigDecimal totalAmount = allPayments.stream()
				.filter(p -> p.getPaymentStatus() == PaymentStatus.paid)
				.map(Payment::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<ReportRequests.PaymentMethodResponse> reports = new ArrayList<>();

		for (Map.Entry<PaymentMethod, List<Payment>> entry : groupedByMethod.entrySet()) {
			List<Payment> methodPayments = entry.getValue();
			int count = methodPayments.size();

			BigDecimal methodTotal = methodPayments.stream()
					.map(Payment::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal percentage = totalPayments > 0
					? methodTotal.divide(totalAmount, 2, java.math.RoundingMode.HALF_UP)
							.multiply(BigDecimal.valueOf(100))
					: BigDecimal.ZERO;

			reports.add(new ReportRequests.PaymentMethodResponse(
					entry.getKey().toString(),
					count,
					methodTotal,
					percentage));
		}

		return reports;
	}

	public ReportRequests.AppointmentStatsResponse getAppointmentStats() {
		List<Appointment> allAppointments = appointmentRepository.findAll();

		return new ReportRequests.AppointmentStatsResponse(
				allAppointments.size(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.pending).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.confirmed).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.completed).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.cancelled).count());
	}
}
