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

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.ServiceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
	private final AppointmentRepository appointmentRepository;
	private final CustomerRepository customerRepository;
	private final ServiceRepository serviceRepository;

	public AppointmentController(
			AppointmentRepository appointmentRepository,
			CustomerRepository customerRepository,
			ServiceRepository serviceRepository) {
		this.appointmentRepository = appointmentRepository;
		this.customerRepository = customerRepository;
		this.serviceRepository = serviceRepository;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<AppointmentRequests.Response> list() {
		return appointmentRepository.findAll().stream().map(AppointmentRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public AppointmentRequests.Response get(@PathVariable Integer id) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
		return AppointmentRequests.Response.from(appointment);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentRequests.Response create(@Valid @RequestBody AppointmentRequests.Create req) {
		Customer customer = customerRepository.findById(req.customerId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customerId"));
		ServiceEntity service = serviceRepository.findById(req.serviceId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceId"));

		Appointment appointment = new Appointment();
		appointment.setCustomer(customer);
		appointment.setService(service);
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setStatus(req.status() != null ? req.status() : AppointmentStatus.pending);
		appointment.setNote(req.note());

		return AppointmentRequests.Response.from(appointmentRepository.save(appointment));
	}

	@PutMapping("/{id}")
	public AppointmentRequests.Response update(@PathVariable Integer id, @Valid @RequestBody AppointmentRequests.Update req) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
		Customer customer = customerRepository.findById(req.customerId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customerId"));
		ServiceEntity service = serviceRepository.findById(req.serviceId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceId"));

		appointment.setCustomer(customer);
		appointment.setService(service);
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setStatus(req.status());
		appointment.setNote(req.note());

		return AppointmentRequests.Response.from(appointmentRepository.save(appointment));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		if (!appointmentRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
		}
		appointmentRepository.deleteById(id);
	}
}
