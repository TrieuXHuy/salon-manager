package com.salonnbooking.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.ScheduleRequests;
import com.salonnbooking.service.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
	private final ScheduleService scheduleService;

	public ScheduleController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@GetMapping("/available-slots")
	@ResponseStatus(HttpStatus.OK)
	public List<ScheduleRequests.AvailableSlotResponse> getAvailableSlots(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam Integer serviceId) {
		return scheduleService.getAvailableSlots(date, serviceId);
	}

	@GetMapping("/by-date")
	@ResponseStatus(HttpStatus.OK)
	public List<ScheduleRequests.AppointmentScheduleResponse> getAppointmentsByDate(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return scheduleService.getAppointmentsByDate(date);
	}

	@GetMapping("/week")
	@ResponseStatus(HttpStatus.OK)
	public List<ScheduleRequests.DayScheduleResponse> getWeekSchedule(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
		return scheduleService.getWeekSchedule(startDate);
	}

	@GetMapping("/month")
	@ResponseStatus(HttpStatus.OK)
	public List<ScheduleRequests.DayScheduleResponse> getMonthSchedule(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startOfMonth) {
		return scheduleService.getMonthSchedule(startOfMonth);
	}
}
