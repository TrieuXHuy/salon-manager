package com.salonnbooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ScheduleRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional(readOnly = true)
public class ScheduleService {
	private final AppointmentRepository appointmentRepository;
	private final ServiceRepository serviceRepository;
	private final CustomerRepository customerRepository;

	private static final int BUSINESS_HOURS_START = 8;
	private static final int BUSINESS_HOURS_END = 18;
	private static final int SLOT_DURATION_MINUTES = 30;

	public ScheduleService(
			AppointmentRepository appointmentRepository,
			ServiceRepository serviceRepository,
			CustomerRepository customerRepository) {
		this.appointmentRepository = appointmentRepository;
		this.serviceRepository = serviceRepository;
		this.customerRepository = customerRepository;
	}

	public List<ScheduleRequests.AvailableSlotResponse> getAvailableSlots(LocalDate date, Integer serviceId) {
		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new RuntimeException("Service not found"));

		List<ScheduleRequests.AvailableSlotResponse> availableSlots = new ArrayList<>();
		List<Appointment> dayAppointments = appointmentRepository
				.findAppointmentsBetween(
						date.atStartOfDay(),
						date.atTime(23, 59, 59));

		for (int hour = BUSINESS_HOURS_START; hour < BUSINESS_HOURS_END; hour++) {
			for (int minute = 0; minute < 60; minute += SLOT_DURATION_MINUTES) {
				LocalDateTime slotTime = date.atTime(hour, minute);

				boolean isAvailable = dayAppointments.stream()
						.noneMatch(apt -> isTimeConflict(apt, slotTime, service.getDurationMinutes()));

				availableSlots.add(new ScheduleRequests.AvailableSlotResponse(
						slotTime,
						isAvailable,
						serviceId));
			}
		}

		return availableSlots;
	}

	public List<ScheduleRequests.AppointmentScheduleResponse> getAppointmentsByDate(LocalDate date) {
		List<Appointment> dayAppointments = appointmentRepository
				.findAppointmentsBetween(
						date.atStartOfDay(),
						date.atTime(23, 59, 59));

		List<ScheduleRequests.AppointmentScheduleResponse> schedules = new ArrayList<>();

		for (Appointment apt : dayAppointments) {
			schedules.add(new ScheduleRequests.AppointmentScheduleResponse(
					apt.getId(),
					apt.getCustomer().getId(),
					apt.getCustomer().getFullName(),
					apt.getService().getId(),
					apt.getService().getName(),
					apt.getAppointmentTime(),
					apt.getService().getDurationMinutes(),
					apt.getStatus(),
					apt.getNote()));
		}

		return schedules.stream()
				.sorted((a, b) -> a.appointmentTime().compareTo(b.appointmentTime()))
				.toList();
	}

	public List<ScheduleRequests.DayScheduleResponse> getWeekSchedule(LocalDate startDate) {
		List<ScheduleRequests.DayScheduleResponse> weekSchedule = new ArrayList<>();

		for (int i = 0; i < 7; i++) {
			LocalDate date = startDate.plusDays(i);
			List<Appointment> dayAppointments = appointmentRepository
					.findAppointmentsBetween(
							date.atStartOfDay(),
							date.atTime(23, 59, 59));

			int totalSlots = (BUSINESS_HOURS_END - BUSINESS_HOURS_START) * (60 / SLOT_DURATION_MINUTES);
			int bookedSlots = dayAppointments.size();
			int availableSlots = totalSlots - bookedSlots;

			weekSchedule.add(new ScheduleRequests.DayScheduleResponse(
					date.atStartOfDay(),
					totalSlots,
					bookedSlots,
					availableSlots));
		}

		return weekSchedule;
	}

	public List<ScheduleRequests.DayScheduleResponse> getMonthSchedule(LocalDate startOfMonth) {
		LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
		List<ScheduleRequests.DayScheduleResponse> monthSchedule = new ArrayList<>();

		for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
			List<Appointment> dayAppointments = appointmentRepository
					.findAppointmentsBetween(
							date.atStartOfDay(),
							date.atTime(23, 59, 59));

			int totalSlots = (BUSINESS_HOURS_END - BUSINESS_HOURS_START) * (60 / SLOT_DURATION_MINUTES);
			int bookedSlots = dayAppointments.size();
			int availableSlots = totalSlots - bookedSlots;

			monthSchedule.add(new ScheduleRequests.DayScheduleResponse(
					date.atStartOfDay(),
					totalSlots,
					bookedSlots,
					availableSlots));
		}

		return monthSchedule;
	}

	private boolean isTimeConflict(Appointment apt, LocalDateTime slotTime, Integer serviceDuration) {
		LocalDateTime aptEnd = apt.getAppointmentTime().plusMinutes(apt.getService().getDurationMinutes());
		LocalDateTime slotEnd = slotTime.plusMinutes(serviceDuration);

		return !slotEnd.isBefore(apt.getAppointmentTime()) && !slotTime.isAfter(aptEnd);
	}
}
