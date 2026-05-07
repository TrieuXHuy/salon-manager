package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.salonnbooking.api.dto.SmsLogRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.SmsLog;
import com.salonnbooking.domain.SmsStatus;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.SmsLogRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sms-logs")
public class SmsLogController {
	private final SmsLogRepository smsLogRepository;
	private final AppointmentRepository appointmentRepository;

	public SmsLogController(SmsLogRepository smsLogRepository, AppointmentRepository appointmentRepository) {
		this.smsLogRepository = smsLogRepository;
		this.appointmentRepository = appointmentRepository;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<SmsLogRequests.Response> list() {
		return smsLogRepository.findAll().stream().map(SmsLogRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public SmsLogRequests.Response get(@PathVariable Integer id) {
		SmsLog log = smsLogRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SmsLog not found"));
		return SmsLogRequests.Response.from(log);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SmsLogRequests.Response create(@Valid @RequestBody SmsLogRequests.Create req) {
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid appointmentId"));

		SmsLog log = new SmsLog();
		log.setAppointment(appointment);
		log.setPhone(req.phone());
		log.setMessage(req.message());
		log.setStatus(req.status() != null ? req.status() : SmsStatus.success);
		return SmsLogRequests.Response.from(smsLogRepository.save(log));
	}

	@PutMapping("/{id}")
	public SmsLogRequests.Response update(@PathVariable Integer id, @Valid @RequestBody SmsLogRequests.Update req) {
		SmsLog log = smsLogRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SmsLog not found"));
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid appointmentId"));

		log.setAppointment(appointment);
		log.setPhone(req.phone());
		log.setMessage(req.message());
		log.setStatus(req.status());
		return SmsLogRequests.Response.from(smsLogRepository.save(log));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		if (!smsLogRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SmsLog not found");
		}
		smsLogRepository.deleteById(id);
	}
}
