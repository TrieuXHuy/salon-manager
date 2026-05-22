package com.salonnbooking.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
	private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
	private static final LocalTime CLOSE_TIME = LocalTime.of(20, 0);

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
		validateAppointmentSlot(null, customer, service, req.appointmentTime());

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
		validateAppointmentSlot(id, customer, service, req.appointmentTime());

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

	private void validateAppointmentSlot(Integer currentAppointmentId, Customer customer, ServiceEntity service,
			LocalDateTime startTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("Appointment time is required");
		}
		if (startTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Cannot create an appointment in the past");
		}
		int duration = service.getDurationMinutes() == null ? 60 : service.getDurationMinutes();
		LocalDateTime endTime = startTime.plusMinutes(duration);
		if (startTime.toLocalTime().isBefore(OPEN_TIME) || endTime.toLocalTime().isAfter(CLOSE_TIME)
				|| !endTime.toLocalDate().equals(startTime.toLocalDate())) {
			throw new IllegalArgumentException("Appointment must be inside opening hours 08:00 - 20:00");
		}
		LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay();
		LocalDateTime dayEnd = startTime.toLocalDate().atTime(23, 59, 59);
		for (Appointment existing : appointmentRepository.findAppointmentsBetween(dayStart, dayEnd)) {
			if (currentAppointmentId != null && currentAppointmentId.equals(existing.getId())) {
				continue;
			}
			if (existing.getStatus() == AppointmentStatus.cancelled || existing.getStatus() == AppointmentStatus.paid) {
				continue;
			}
			LocalDateTime existingStart = existing.getAppointmentTime();
			int existingDuration = existing.getService().getDurationMinutes() == null ? 60
					: existing.getService().getDurationMinutes();
			LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);
			boolean overlap = startTime.isBefore(existingEnd) && endTime.isAfter(existingStart);
			if (!overlap) {
				continue;
			}
			if (existing.getCustomer().getId().equals(customer.getId())) {
				throw new IllegalArgumentException("Customer already has another appointment in this time slot");
			}
			if (existing.getService().getId().equals(service.getId())) {
				throw new IllegalArgumentException("This service already has another appointment in this time slot");
			}
		}
	}
}
