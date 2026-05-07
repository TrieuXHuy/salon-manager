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

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<AppointmentRequests.Response> list() {
		return appointmentService.findAll().stream().map(AppointmentRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public AppointmentRequests.Response get(@PathVariable Integer id) {
		Appointment appointment = appointmentService.findById(id);
		return AppointmentRequests.Response.from(appointment);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentRequests.Response create(@Valid @RequestBody AppointmentRequests.Create req) {
		Appointment appointment = appointmentService.save(req);
		return AppointmentRequests.Response.from(appointment);
	}

	@PutMapping("/{id}")
	public AppointmentRequests.Response update(@PathVariable Integer id,
			@Valid @RequestBody AppointmentRequests.Update req) {
		Appointment appointment = appointmentService.update(id, req);
		return AppointmentRequests.Response.from(appointment);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		appointmentService.delete(id);
	}
}
