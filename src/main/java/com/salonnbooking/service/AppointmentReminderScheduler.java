package com.salonnbooking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.repository.AppointmentRepository;

@Service
public class AppointmentReminderScheduler {
	private static final Logger log = LoggerFactory.getLogger(AppointmentReminderScheduler.class);
	private static final int REMINDER_WINDOW_MINUTES = 30;

	private final AppointmentRepository appointmentRepository;
	private final EmailService emailService;

	public AppointmentReminderScheduler(AppointmentRepository appointmentRepository, EmailService emailService) {
		this.appointmentRepository = appointmentRepository;
		this.emailService = emailService;
	}

	@Scheduled(cron = "0 0/30 * * * *")
	public void remindUpcomingConfirmedAppointments() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime windowEnd = now.plusMinutes(REMINDER_WINDOW_MINUTES);
		// Lấy các lịch hẹn đã xác nhận và sắp diễn ra trong khoảng thời gian nhắc nhở.
		List<Appointment> appointments = appointmentRepository.findByStatusAndAppointmentTimeBetween(
				AppointmentStatus.confirmed,
				now,
				windowEnd);

		if (appointments.isEmpty()) {
			log.info("No confirmed appointments to remind between {} and {}", now, windowEnd);   
			return;
		}

		for (Appointment appointment : appointments) {
			try {
				emailService.sendAppointmentReminder(appointment);
				log.info("Automatic reminder sent. appointmentId={}", appointment.getId());
			} catch (Exception e) {
				log.error("Automatic reminder failed. appointmentId={}", appointment.getId(), e);
			}
		}
	}
}
