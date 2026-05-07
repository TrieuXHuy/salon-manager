package com.salonnbooking.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AnalyticsRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {
	private final CustomerRepository customerRepository;
	private final AppointmentRepository appointmentRepository;
	private final ServiceRepository serviceRepository;

	public AnalyticsService(
			CustomerRepository customerRepository,
			AppointmentRepository appointmentRepository,
			ServiceRepository serviceRepository) {
		this.customerRepository = customerRepository;
		this.appointmentRepository = appointmentRepository;
		this.serviceRepository = serviceRepository;
	}

	public AnalyticsRequests.CustomerAnalyticsResponse getCustomerAnalytics() {
		List<Customer> allCustomers = customerRepository.findAll();
		int totalCustomers = allCustomers.size();

		LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
		int newCustomersThisMonth = (int) allCustomers.stream()
				.filter(c -> c.getCreatedAt().toLocalDate().isAfter(monthStart))
				.count();

		int activeCustomers = (int) allCustomers.stream()
				.filter(c -> c.getId() != null)
				.count();

		int inactiveCustomers = totalCustomers - activeCustomers;

		double retentionRate = totalCustomers > 0
				? (double) activeCustomers / totalCustomers * 100
				: 0;

		return new AnalyticsRequests.CustomerAnalyticsResponse(
				totalCustomers,
				newCustomersThisMonth,
				activeCustomers,
				inactiveCustomers,
				retentionRate);
	}

	public List<AnalyticsRequests.AppointmentTrendResponse> getAppointmentTrends(LocalDate startDate,
			LocalDate endDate) {
		List<Appointment> appointments = appointmentRepository
				.findAppointmentsBetween(
						startDate.atStartOfDay(),
						endDate.atTime(23, 59, 59));

		Map<LocalDate, List<Appointment>> groupedByDate = appointments.stream()
				.collect(Collectors.groupingBy(a -> a.getAppointmentTime().toLocalDate()));

		List<AnalyticsRequests.AppointmentTrendResponse> trends = new ArrayList<>();

		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			List<Appointment> dayAppointments = groupedByDate.getOrDefault(date, new ArrayList<>());
			int completed = (int) dayAppointments.stream()
					.filter(a -> a.getStatus() == AppointmentStatus.completed)
					.count();
			int cancelled = (int) dayAppointments.stream()
					.filter(a -> a.getStatus() == AppointmentStatus.cancelled)
					.count();

			trends.add(new AnalyticsRequests.AppointmentTrendResponse(
					date,
					dayAppointments.size(),
					completed,
					cancelled));
		}

		return trends;
	}

	public List<AnalyticsRequests.ServicePerformanceResponse> getServicePerformance() {
		List<Appointment> allAppointments = appointmentRepository.findAll();

		Map<Integer, List<Appointment>> groupedByService = allAppointments.stream()
				.collect(Collectors.groupingBy(a -> a.getService().getId()));

		List<AnalyticsRequests.ServicePerformanceResponse> performances = new ArrayList<>();

		for (Map.Entry<Integer, List<Appointment>> entry : groupedByService.entrySet()) {
			List<Appointment> serviceAppointments = entry.getValue();
			int bookingCount = serviceAppointments.size();

			int totalAppointments = allAppointments.size();
			double popularity = totalAppointments > 0
					? (double) bookingCount / totalAppointments * 100
					: 0;

			String serviceName = serviceAppointments.get(0).getService().getName();

			double rating = 4.5; // Default rating, có thể lấy từ reviews

			performances.add(new AnalyticsRequests.ServicePerformanceResponse(
					entry.getKey(),
					serviceName,
					bookingCount,
					popularity,
					rating));
		}

		return performances.stream()
				.sorted((a, b) -> Double.compare(b.popularity(), a.popularity()))
				.collect(Collectors.toList());
	}

	public List<AnalyticsRequests.PeakHoursResponse> getPeakHours() {
		List<Appointment> allAppointments = appointmentRepository.findAll();

		Map<Integer, Long> hourlyCount = allAppointments.stream()
				.collect(Collectors.groupingBy(
						a -> a.getAppointmentTime().getHour(),
						Collectors.counting()));

		List<AnalyticsRequests.PeakHoursResponse> peakHours = new ArrayList<>();

		for (int hour = 0; hour < 24; hour++) {
			int count = hourlyCount.getOrDefault(hour, 0L).intValue();
			String hourStr = String.format("%02d:00", hour);

			peakHours.add(new AnalyticsRequests.PeakHoursResponse(hourStr, count));
		}

		return peakHours;
	}
}
