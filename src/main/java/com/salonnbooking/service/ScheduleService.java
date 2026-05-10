package com.salonnbooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ScheduleRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentServiceItem;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional(readOnly = true)
public class ScheduleService {
	private final AppointmentRepository appointmentRepository;
	private final ServiceRepository serviceRepository;

	private static final int BUSINESS_HOURS_START = 8;
	private static final int BUSINESS_HOURS_END = 18;
	private static final int SLOT_DURATION_MINUTES = 30;

	public ScheduleService(
			AppointmentRepository appointmentRepository,
			ServiceRepository serviceRepository) {
		this.appointmentRepository = appointmentRepository;
		this.serviceRepository = serviceRepository;
	}

	public List<ScheduleRequests.AvailableSlotResponse> getAvailableSlots(LocalDate date, Integer serviceId) {
		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new RuntimeException("Service not found"));

		List<ScheduleRequests.AvailableSlotResponse> availableSlots = new ArrayList<>();
		List<Appointment> dayAppointments = appointmentRepository
				.findAppointmentsBetween(date.atStartOfDay(), date.atTime(23, 59, 59));

		for (int hour = BUSINESS_HOURS_START; hour < BUSINESS_HOURS_END; hour++) {
			for (int minute = 0; minute < 60; minute += SLOT_DURATION_MINUTES) {
				LocalDateTime slotTime = date.atTime(hour, minute);
				boolean isAvailable = dayAppointments.stream()
						.filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED && apt.getStatus() != AppointmentStatus.NO_SHOW)
						.noneMatch(apt -> isTimeConflict(apt, slotTime, service.getDurationMinutes()));
				availableSlots.add(new ScheduleRequests.AvailableSlotResponse(slotTime, isAvailable, serviceId));
			}
		}

		return availableSlots;
	}

	public List<ScheduleRequests.AppointmentScheduleResponse> getAppointmentsByDate(LocalDate date) {
		List<Appointment> dayAppointments = appointmentRepository
				.findAppointmentsBetween(date.atStartOfDay(), date.atTime(23, 59, 59));

		return dayAppointments.stream()
				.sorted((a, b) -> a.getAppointmentTime().compareTo(b.getAppointmentTime()))
				.map(apt -> new ScheduleRequests.AppointmentScheduleResponse(
						apt.getId(),
						apt.getCustomer().getId(),
						apt.getCustomer().getFullName(),
						apt.getEmployee() != null ? apt.getEmployee().getId() : null,
						apt.getEmployee() != null ? apt.getEmployee().getFullName() : "Chua phan cong",
						apt.getAppointmentTime(),
						totalDuration(apt),
						apt.getStatus(),
						serviceSummary(apt),
						apt.getNote()))
				.toList();
	}

	public List<ScheduleRequests.DayScheduleResponse> getWeekSchedule(LocalDate startDate) {
		List<ScheduleRequests.DayScheduleResponse> weekSchedule = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			LocalDate date = startDate.plusDays(i);
			weekSchedule.add(buildDaySchedule(date));
		}
		return weekSchedule;
	}

	public List<ScheduleRequests.DayScheduleResponse> getMonthSchedule(LocalDate startOfMonth) {
		LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
		List<ScheduleRequests.DayScheduleResponse> monthSchedule = new ArrayList<>();
		for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
			monthSchedule.add(buildDaySchedule(date));
		}
		return monthSchedule;
	}

	private ScheduleRequests.DayScheduleResponse buildDaySchedule(LocalDate date) {
		List<Appointment> dayAppointments = appointmentRepository
				.findAppointmentsBetween(date.atStartOfDay(), date.atTime(23, 59, 59));
		int totalSlots = (BUSINESS_HOURS_END - BUSINESS_HOURS_START) * (60 / SLOT_DURATION_MINUTES);
		int bookedSlots = dayAppointments.size();
		int availableSlots = totalSlots - bookedSlots;
		return new ScheduleRequests.DayScheduleResponse(date.atStartOfDay(), totalSlots, bookedSlots, availableSlots);
	}

	private boolean isTimeConflict(Appointment apt, LocalDateTime slotTime, Integer serviceDuration) {
		LocalDateTime slotEnd = slotTime.plusMinutes(serviceDuration);
		LocalDateTime appointmentEnd = apt.getEstimatedEndTime() != null
				? apt.getEstimatedEndTime()
				: apt.getAppointmentTime().plusMinutes(Math.max(totalDuration(apt), serviceDuration));
		return slotTime.isBefore(appointmentEnd) && slotEnd.isAfter(apt.getAppointmentTime());
	}

	private Integer totalDuration(Appointment appointment) {
		return appointment.getAppointmentServices().stream()
				.mapToInt(AppointmentServiceItem::getDurationMinutes)
				.sum();
	}

	private String serviceSummary(Appointment appointment) {
		return appointment.getAppointmentServices().stream()
				.map(AppointmentServiceItem::getServiceNameSnapshot)
				.reduce((left, right) -> left + ", " + right)
				.orElse("");
	}
}
