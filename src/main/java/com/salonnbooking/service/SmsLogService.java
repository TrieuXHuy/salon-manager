package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.SmsLogRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.SmsLog;
import com.salonnbooking.domain.SmsStatus;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.SmsLogRepository;

@Service
@Transactional
public class SmsLogService {
	private final SmsLogRepository smsLogRepository;
	private final AppointmentRepository appointmentRepository;

	public SmsLogService(SmsLogRepository smsLogRepository, AppointmentRepository appointmentRepository) {
		this.smsLogRepository = smsLogRepository;
		this.appointmentRepository = appointmentRepository;
	}

	@Transactional(readOnly = true)
	public List<SmsLog> findAll() {
		return smsLogRepository.findAll();
	}

	@Transactional(readOnly = true)
	public SmsLog findById(Integer id) {
		return smsLogRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SmsLog not found with id: " + id));
	}

	public SmsLog save(SmsLogRequests.Create req) {
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + req.appointmentId()));

		SmsLog log = new SmsLog();
		log.setAppointment(appointment);
		log.setPhone(req.phone());
		log.setMessage(req.message());
		log.setStatus(req.status() != null ? req.status() : SmsStatus.success);
		return smsLogRepository.save(log);
	}

	public SmsLog update(Integer id, SmsLogRequests.Update req) {
		SmsLog log = findById(id);
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + req.appointmentId()));

		log.setAppointment(appointment);
		log.setPhone(req.phone());
		log.setMessage(req.message());
		log.setStatus(req.status());
		return smsLogRepository.save(log);
	}

	public void delete(Integer id) {
		if (!smsLogRepository.existsById(id)) {
			throw new ResourceNotFoundException("SmsLog not found with id: " + id);
		}
		smsLogRepository.deleteById(id);
	}
}
