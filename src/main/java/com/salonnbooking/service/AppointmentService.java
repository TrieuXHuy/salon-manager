package com.salonnbooking.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional
public class AppointmentService {
	private final AppointmentRepository appointmentRepository;
	private final CustomerRepository customerRepository;
	private final ServiceRepository serviceRepository;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			CustomerRepository customerRepository,
			ServiceRepository serviceRepository) {
		this.appointmentRepository = appointmentRepository;
		this.customerRepository = customerRepository;
		this.serviceRepository = serviceRepository;
	}

	@Transactional(readOnly = true)
	public List<Appointment> findAll() {
		return appointmentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
	}

	@Transactional(readOnly = true)
	public Appointment findById(Integer id) {
		return appointmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
	}

	public Appointment save(AppointmentRequests.Create req) {
		Customer customer = customerRepository.findById(req.customerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + req.customerId()));
		
		// For now, use the first service ID (backend only supports one service per appointment)
		Integer serviceId = req.serviceIds() != null && !req.serviceIds().isEmpty() ? req.serviceIds().get(0) : null;
		if (serviceId == null) {
			throw new ResourceNotFoundException("No service selected");
		}
		
		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

		Appointment appointment = new Appointment();
		appointment.setCustomer(customer);
		appointment.setService(service);
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setStatus(req.status() != null ? req.status() : AppointmentStatus.pending);
		appointment.setNote(req.note());

		return appointmentRepository.save(appointment);
	}

	public Appointment update(Integer id, AppointmentRequests.Update req) {
		Appointment appointment = findById(id);
		Customer customer = customerRepository.findById(req.customerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + req.customerId()));
		
		// For now, use the first service ID (backend only supports one service per appointment)
		Integer serviceId = req.serviceIds() != null && !req.serviceIds().isEmpty() ? req.serviceIds().get(0) : null;
		if (serviceId == null) {
			throw new ResourceNotFoundException("No service selected");
		}
		
		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

		appointment.setCustomer(customer);
		appointment.setService(service);
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setStatus(req.status());
		appointment.setNote(req.note());

		return appointmentRepository.save(appointment);
	}

	public void delete(Integer id) {
		if (!appointmentRepository.existsById(id)) {
			throw new ResourceNotFoundException("Appointment not found with id: " + id);
		}
		appointmentRepository.deleteById(id);
	}
}
