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
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.domain.ServiceRoom;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.ServiceRoomRepository;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional(readOnly = true)
public class ScheduleService {
	private final AppointmentRepository appointmentRepository;
	private final ServiceRepository serviceRepository;
	private final CustomerRepository customerRepository;
	private final ServiceRoomRepository serviceRoomRepository;

	private static final int BUSINESS_HOURS_START = 8;
	private static final int BUSINESS_HOURS_END = 20;
	private static final int SLOT_DURATION_MINUTES = 30;

	public ScheduleService(
			AppointmentRepository appointmentRepository,
			ServiceRepository serviceRepository,
			CustomerRepository customerRepository,
			ServiceRoomRepository serviceRoomRepository) {
		this.appointmentRepository = appointmentRepository;
		this.serviceRepository = serviceRepository;
		this.customerRepository = customerRepository;
		this.serviceRoomRepository = serviceRoomRepository;
	}

	public List<ScheduleRequests.AvailableSlotResponse> getAvailableSlots(LocalDate date, Integer serviceId) {
		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new RuntimeException("Service not found"));

		List<ScheduleRequests.AvailableSlotResponse> availableSlots = new ArrayList<>();
		List<Appointment> dayAppointments = appointmentRepository
				.findAppointmentsBetween(
						date.atStartOfDay(),
						date.atTime(23, 59, 59));
		List<ServiceRoom> rooms = serviceRoomRepository.findByIsActiveTrueOrderByIdAsc();
		int duration = roundedDuration(service.getDurationMinutes());

		for (int hour = BUSINESS_HOURS_START; hour < BUSINESS_HOURS_END; hour++) {
			for (int minute = 0; minute < 60; minute += SLOT_DURATION_MINUTES) {
				LocalDateTime slotTime = date.atTime(hour, minute);
				if (slotTime.plusMinutes(duration).toLocalTime().isAfter(LocalTime.of(BUSINESS_HOURS_END, 0))) {
					continue;
				}
				for (ServiceRoom room : rooms) {
					boolean isAvailable = dayAppointments.stream()
							.noneMatch(apt -> isRoomConflict(apt, room, slotTime, duration));
					availableSlots.add(new ScheduleRequests.AvailableSlotResponse(
							slotTime,
							isAvailable,
							serviceId,
							room.getId(),
							room.getName(),
							duration,
							isAvailable ? 0 : 1,
							1));
				}
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
					apt.getRoom() == null ? null : apt.getRoom().getId(),
					apt.getRoom() == null ? "" : apt.getRoom().getName(),
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

	private boolean isRoomConflict(Appointment apt, ServiceRoom room, LocalDateTime slotTime, Integer serviceDuration) {
		if (apt.getStatus() == AppointmentStatus.pending || apt.getStatus() == AppointmentStatus.cancelled) {
			return false;
		}
		if (apt.getStatus() != AppointmentStatus.confirmed
				&& apt.getStatus() != AppointmentStatus.in_progress
				&& apt.getStatus() != AppointmentStatus.awaiting_payment
				&& apt.getStatus() != AppointmentStatus.completed
				&& apt.getStatus() != AppointmentStatus.paid) {
			return false;
		}
		if (apt.getRoom() == null || !apt.getRoom().getId().equals(room.getId())) {
			return false;
		}
		LocalDateTime aptEnd = apt.getAppointmentTime().plusMinutes(roundedDuration(apt.getService().getDurationMinutes()));
		LocalDateTime slotEnd = slotTime.plusMinutes(serviceDuration);

		return slotTime.isBefore(aptEnd) && slotEnd.isAfter(apt.getAppointmentTime());
	}

	private int roundedDuration(Integer durationMinutes) {
		int duration = durationMinutes == null || durationMinutes <= 0 ? SLOT_DURATION_MINUTES : durationMinutes;
		return ((duration + SLOT_DURATION_MINUTES - 1) / SLOT_DURATION_MINUTES) * SLOT_DURATION_MINUTES;
	}
}
