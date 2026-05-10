package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentServiceItem;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.AppointmentStatusHistory;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.domain.Employee;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.AppointmentStatusHistoryRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.EmployeeRepository;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional
public class AppointmentService {
	private static final List<AppointmentStatus> INACTIVE_STATUSES = List.of(
			AppointmentStatus.CANCELLED,
			AppointmentStatus.NO_SHOW);

	private final AppointmentRepository appointmentRepository;
	private final AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;
	private final CustomerRepository customerRepository;
	private final EmployeeRepository employeeRepository;
	private final ServiceRepository serviceRepository;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			AppointmentStatusHistoryRepository appointmentStatusHistoryRepository,
			CustomerRepository customerRepository,
			EmployeeRepository employeeRepository,
			ServiceRepository serviceRepository) {
		this.appointmentRepository = appointmentRepository;
		this.appointmentStatusHistoryRepository = appointmentStatusHistoryRepository;
		this.customerRepository = customerRepository;
		this.employeeRepository = employeeRepository;
		this.serviceRepository = serviceRepository;
	}

	@Transactional(readOnly = true)
	public List<Appointment> findAll() {
		return appointmentRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Appointment findById(Integer id) {
		return appointmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
	}

	public Appointment save(AppointmentRequests.Create req) {
		Customer customer = findCustomer(req.customerId());
		Employee employee = findEmployee(req.employeeId());
		List<ServiceEntity> services = findServices(req.serviceIds());
		AppointmentStatus targetStatus = req.status() != null ? req.status() : AppointmentStatus.PENDING;

		LocalDateTime estimatedEndTime = calculateEstimatedEndTime(req.appointmentTime(), services);
		validateNoOverlap(employee.getId(), req.appointmentTime(), estimatedEndTime, null);
		validateStatusTransition(null, targetStatus);

		Appointment appointment = new Appointment();
		appointment.setCustomer(customer);
		appointment.setEmployee(employee);
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setEstimatedEndTime(estimatedEndTime);
		appointment.setStatus(targetStatus);
		appointment.setNote(req.note());
		applyStatusTimestamps(appointment, targetStatus);
		rebuildServiceItems(appointment, services, employee);

		Appointment saved = appointmentRepository.save(appointment);
		recordStatusHistory(saved, null, saved.getStatus(), "Created");
		return saved;
	}

	public Appointment update(Integer id, AppointmentRequests.Update req) {
		Appointment appointment = findById(id);
		Customer customer = findCustomer(req.customerId());
		Employee employee = findEmployee(req.employeeId());
		List<ServiceEntity> services = findServices(req.serviceIds());

		LocalDateTime estimatedEndTime = calculateEstimatedEndTime(req.appointmentTime(), services);
		validateNoOverlap(employee.getId(), req.appointmentTime(), estimatedEndTime, appointment.getId());
		validateStatusTransition(appointment.getStatus(), req.status());

		AppointmentStatus previousStatus = appointment.getStatus();
		appointment.setCustomer(customer);
		appointment.setEmployee(employee);
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setEstimatedEndTime(estimatedEndTime);
		appointment.setStatus(req.status());
		appointment.setNote(req.note());
		applyStatusTimestamps(appointment, req.status());
		rebuildServiceItems(appointment, services, employee);

		Appointment saved = appointmentRepository.save(appointment);
		if (previousStatus != saved.getStatus()) {
			recordStatusHistory(saved, previousStatus, saved.getStatus(), "Updated from appointment form");
		}
		return saved;
	}

	public void delete(Integer id) {
		if (!appointmentRepository.existsById(id)) {
			throw new ResourceNotFoundException("Appointment not found with id: " + id);
		}
		appointmentRepository.deleteById(id);
	}

	private Customer findCustomer(Integer customerId) {
		return customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
	}

	private Employee findEmployee(Integer employeeId) {
		return employeeRepository.findById(employeeId)
				.orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
	}

	private List<ServiceEntity> findServices(List<Integer> serviceIds) {
		List<ServiceEntity> foundServices = serviceRepository.findAllById(serviceIds);
		if (foundServices.size() != serviceIds.size()) {
			throw new ResourceNotFoundException("One or more services were not found");
		}
		Map<Integer, ServiceEntity> serviceMap = foundServices.stream()
				.collect(Collectors.toMap(ServiceEntity::getId, Function.identity()));
		List<ServiceEntity> ordered = new ArrayList<>();
		for (Integer serviceId : serviceIds) {
			ServiceEntity service = serviceMap.get(serviceId);
			if (service == null) {
				throw new ResourceNotFoundException("Service not found with id: " + serviceId);
			}
			ordered.add(service);
		}
		return ordered;
	}

	private LocalDateTime calculateEstimatedEndTime(LocalDateTime appointmentTime, List<ServiceEntity> services) {
		int totalMinutes = services.stream()
				.mapToInt(ServiceEntity::getDurationMinutes)
				.sum();
		return appointmentTime.plusMinutes(totalMinutes);
	}

	private void validateNoOverlap(Integer employeeId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeId) {
		List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
				employeeId,
				startTime,
				endTime,
				INACTIVE_STATUSES,
				excludeId);
		if (!conflicts.isEmpty()) {
			throw new IllegalArgumentException("Employee already has an overlapping appointment in this time range");
		}
	}

	private void validateStatusTransition(AppointmentStatus currentStatus, AppointmentStatus newStatus) {
		if (currentStatus == null) {
			if (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.IN_PROGRESS
					|| newStatus == AppointmentStatus.CHECKED_IN) {
				throw new IllegalArgumentException("New appointment cannot start directly at this status");
			}
			return;
		}

		EnumSet<AppointmentStatus> allowed = switch (currentStatus) {
			case PENDING -> EnumSet.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
			case CONFIRMED -> EnumSet.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
			case CHECKED_IN -> EnumSet.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED);
			case IN_PROGRESS -> EnumSet.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED);
			case COMPLETED, CANCELLED, NO_SHOW -> EnumSet.noneOf(AppointmentStatus.class);
		};

		if (currentStatus != newStatus && !allowed.contains(newStatus)) {
			throw new IllegalArgumentException("Invalid appointment status transition: " + currentStatus + " -> " + newStatus);
		}
	}

	private void applyStatusTimestamps(Appointment appointment, AppointmentStatus status) {
		switch (status) {
			case CHECKED_IN -> {
				if (appointment.getCheckInAt() == null) {
					appointment.setCheckInAt(LocalDateTime.now());
				}
			}
			case IN_PROGRESS -> {
				if (appointment.getCheckInAt() == null) {
					appointment.setCheckInAt(LocalDateTime.now());
				}
				if (appointment.getStartedAt() == null) {
					appointment.setStartedAt(LocalDateTime.now());
				}
			}
			case COMPLETED -> {
				if (appointment.getCheckInAt() == null) {
					appointment.setCheckInAt(LocalDateTime.now());
				}
				if (appointment.getStartedAt() == null) {
					appointment.setStartedAt(LocalDateTime.now());
				}
				if (appointment.getCompletedAt() == null) {
					appointment.setCompletedAt(LocalDateTime.now());
				}
			}
			case CANCELLED -> {
				if (appointment.getCancelledAt() == null) {
					appointment.setCancelledAt(LocalDateTime.now());
				}
			}
			case PENDING, CONFIRMED, NO_SHOW -> {
			}
		}
	}

	private void rebuildServiceItems(Appointment appointment, List<ServiceEntity> services, Employee employee) {
		appointment.getAppointmentServices().clear();
		int lineNo = 1;
		for (ServiceEntity service : services) {
			AppointmentServiceItem item = new AppointmentServiceItem();
			item.setAppointment(appointment);
			item.setService(service);
			item.setAssignedEmployee(employee);
			item.setLineNo(lineNo++);
			item.setServiceNameSnapshot(service.getName());
			item.setPrice(service.getPrice() == null ? BigDecimal.ZERO : service.getPrice());
			item.setDurationMinutes(service.getDurationMinutes());
			item.setStatus(appointment.getStatus().name());
			appointment.getAppointmentServices().add(item);
		}
	}

	private void recordStatusHistory(Appointment appointment, AppointmentStatus fromStatus, AppointmentStatus toStatus,
			String note) {
		AppointmentStatusHistory history = new AppointmentStatusHistory();
		history.setAppointment(appointment);
		history.setFromStatus(fromStatus);
		history.setToStatus(toStatus);
		history.setNote(note);
		appointmentStatusHistoryRepository.save(history);
	}
}
