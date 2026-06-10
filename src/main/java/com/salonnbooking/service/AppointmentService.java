package com.salonnbooking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.salonnbooking.domain.PaymentStage;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.domain.ServiceRoom;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.PaymentRepository;
import com.salonnbooking.repository.ServiceRepository;
import com.salonnbooking.repository.ServiceRoomRepository;

@Service
@Transactional
public class AppointmentService {
	private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
	private static final LocalTime CLOSE_TIME = LocalTime.of(20, 0);

	private final AppointmentRepository appointmentRepository;
	private final CustomerRepository customerRepository;
	private final ServiceRepository serviceRepository;
	private final ServiceRoomRepository serviceRoomRepository;
	private final PaymentRepository paymentRepository;
	private final EmailService emailService;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			CustomerRepository customerRepository,
			ServiceRepository serviceRepository,
			ServiceRoomRepository serviceRoomRepository,
			PaymentRepository paymentRepository,
			EmailService emailService) {
		this.appointmentRepository = appointmentRepository;
		this.customerRepository = customerRepository;
		this.serviceRepository = serviceRepository;
		this.serviceRoomRepository = serviceRoomRepository;
		this.paymentRepository = paymentRepository;
		this.emailService = emailService;
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

		Integer serviceId = req.serviceIds() != null && !req.serviceIds().isEmpty() ? req.serviceIds().get(0) : null;
		if (serviceId == null) {
			throw new ResourceNotFoundException("No service selected");
		}

		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
		validateBasicTime(service, req.appointmentTime());

		AppointmentStatus status = req.status() == null ? AppointmentStatus.pending : req.status();
		validateCreateStatus(status);

		Appointment appointment = new Appointment();
		appointment.setCustomer(customer);
		appointment.setService(service);
		appointment.setRoom(resolveRoomOnCreate(status, req.roomId(), customer, service, req.appointmentTime()));
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setStatus(status);
		appointment.setNote(req.note());
		recalculateFinancialSnapshot(appointment);
		return appointmentRepository.save(appointment);
	}

	public Appointment update(Integer id, AppointmentRequests.Update req) {
		Appointment appointment = findById(id);
		Customer customer = customerRepository.findById(req.customerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + req.customerId()));

		Integer serviceId = req.serviceIds() != null && !req.serviceIds().isEmpty() ? req.serviceIds().get(0) : null;
		if (serviceId == null) {
			throw new ResourceNotFoundException("No service selected");
		}

		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
		validateBasicTime(service, req.appointmentTime());

		boolean hasPaidPayment = paymentRepository.existsByAppointmentIdAndPaymentStatus(id, PaymentStatus.paid);
		if (hasPaidPayment && hasCoreFieldChanged(appointment, customer, service, req.appointmentTime(), req.roomId())) {
			throw new IllegalArgumentException("Confirmed appointment cannot be rescheduled after payment");
		}

		validateUpdateStatus(appointment, req.status(), hasPaidPayment);

		appointment.setCustomer(customer);
		appointment.setService(service);
		appointment.setRoom(resolveRoomOnUpdate(id, req.status(), req.roomId(), customer, service, req.appointmentTime()));
		appointment.setAppointmentTime(req.appointmentTime());
		appointment.setStatus(req.status());
		appointment.setNote(req.note());
		recalculateFinancialSnapshot(appointment);
		return appointmentRepository.save(appointment);
	}

	public void delete(Integer id) {
		if (!appointmentRepository.existsById(id)) {
			throw new ResourceNotFoundException("Appointment not found with id: " + id);
		}
		appointmentRepository.deleteById(id);
	}

	public void sendReminder(Integer id) {
		Appointment appointment = findById(id);
		if (appointment.getStatus() == AppointmentStatus.pending) {
			throw new IllegalArgumentException("Cannot send reminder before deposit is confirmed");
		}
		emailService.sendAppointmentReminder(appointment);
	}

	private void validateCreateStatus(AppointmentStatus status) {
		if (status == AppointmentStatus.confirmed || status == AppointmentStatus.paid) {
			throw new IllegalArgumentException("Use the payment flow to confirm or pay an appointment");
		}
	}

	private void validateUpdateStatus(Appointment appointment, AppointmentStatus requestedStatus, boolean hasPaidPayment) {
		if (requestedStatus == null) {
			throw new IllegalArgumentException("Appointment status is required");
		}
		if (appointment.getStatus() == AppointmentStatus.paid && requestedStatus != AppointmentStatus.paid) {
			throw new IllegalArgumentException("Paid appointment cannot be changed to another status");
		}
		if (requestedStatus == AppointmentStatus.confirmed && !hasPaidPayment) {
			throw new IllegalArgumentException("Use the deposit payment flow to confirm the appointment");
		}
		if (requestedStatus == AppointmentStatus.paid && safeAmount(appointment.getRemainingAmount()).compareTo(BigDecimal.ZERO) > 0) {
			throw new IllegalArgumentException("Appointment cannot be marked paid before the remaining amount is collected");
		}
	}

	private ServiceRoom resolveRoomOnCreate(AppointmentStatus status, Integer requestedRoomId, Customer customer,
			ServiceEntity service, LocalDateTime startTime) {
		if (requestedRoomId == null) {
			return null;
		}
		ServiceRoom room = serviceRoomRepository.findById(requestedRoomId)
				.orElseThrow(() -> new ResourceNotFoundException("Service room not found with id: " + requestedRoomId));
		if (status != AppointmentStatus.pending) {
			validateAppointmentSlot(null, customer, service, room, startTime);
		}
		return room;
	}

	private ServiceRoom resolveRoomOnUpdate(Integer currentAppointmentId, AppointmentStatus status, Integer requestedRoomId,
			Customer customer, ServiceEntity service, LocalDateTime startTime) {
		if (requestedRoomId == null) {
			return null;
		}
		ServiceRoom room = serviceRoomRepository.findById(requestedRoomId)
				.orElseThrow(() -> new ResourceNotFoundException("Service room not found with id: " + requestedRoomId));
		if (status != AppointmentStatus.pending) {
			validateAppointmentSlot(currentAppointmentId, customer, service, room, startTime);
		}
		return room;
	}

	private void validateBasicTime(ServiceEntity service, LocalDateTime startTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("Appointment time is required");
		}
		if (startTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Cannot create an appointment in the past");
		}
		int duration = roundedDuration(service.getDurationMinutes());
		LocalDateTime endTime = startTime.plusMinutes(duration);
		if (startTime.toLocalTime().isBefore(OPEN_TIME) || endTime.toLocalTime().isAfter(CLOSE_TIME)
				|| !endTime.toLocalDate().equals(startTime.toLocalDate())) {
			throw new IllegalArgumentException("Appointment must be inside opening hours 08:00 - 20:00");
		}
	}

	private void validateAppointmentSlot(Integer currentAppointmentId, Customer customer, ServiceEntity service,
			ServiceRoom room, LocalDateTime startTime) {
		if (!isRoomAvailable(currentAppointmentId, customer, service, room, startTime)) {
			throw new IllegalArgumentException(room.getName() + " is already booked in this time slot");
		}
	}

	private boolean isRoomAvailable(Integer currentAppointmentId, Customer customer, ServiceEntity service,
			ServiceRoom room, LocalDateTime startTime) {
		int duration = roundedDuration(service.getDurationMinutes());
		LocalDateTime endTime = startTime.plusMinutes(duration);
		LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay();
		LocalDateTime dayEnd = startTime.toLocalDate().atTime(23, 59, 59);
		for (Appointment existing : appointmentRepository.findAppointmentsBetween(dayStart, dayEnd)) {
			if (currentAppointmentId != null && currentAppointmentId.equals(existing.getId())) {
				continue;
			}
			if (!isBlockingStatus(existing.getStatus())) {
				continue;
			}
			if (existing.getRoom() == null || !existing.getRoom().getId().equals(room.getId())) {
				continue;
			}
			LocalDateTime existingStart = existing.getAppointmentTime();
			int existingDuration = roundedDuration(existing.getService().getDurationMinutes());
			LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);
			boolean overlap = startTime.isBefore(existingEnd) && endTime.isAfter(existingStart);
			if (overlap) {
				return false;
			}
		}
		return true;
	}

	private boolean isBlockingStatus(AppointmentStatus status) {
		return status == AppointmentStatus.confirmed
				|| status == AppointmentStatus.completed
				|| status == AppointmentStatus.paid;
	}

	private boolean hasCoreFieldChanged(Appointment appointment, Customer customer, ServiceEntity service,
			LocalDateTime appointmentTime, Integer roomId) {
		boolean customerChanged = !appointment.getCustomer().getId().equals(customer.getId());
		boolean serviceChanged = !appointment.getService().getId().equals(service.getId());
		boolean timeChanged = !appointment.getAppointmentTime().equals(appointmentTime);
		boolean roomChanged = !sameRoomId(appointment.getRoom(), roomId);
		return customerChanged || serviceChanged || timeChanged || roomChanged;
	}

	private boolean sameRoomId(ServiceRoom room, Integer roomId) {
		if (room == null || roomId == null) {
			return room == null && roomId == null;
		}
		return room.getId().equals(roomId);
	}

	private void recalculateFinancialSnapshot(Appointment appointment) {
		BigDecimal total = safeAmount(appointment.getService() == null ? BigDecimal.ZERO : appointment.getService().getPrice());
		BigDecimal deposit = total.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);
		BigDecimal amountPaid = paymentRepository.findByAppointmentId(appointment.getId()).stream()
				.filter(payment -> payment.getPaymentStatus() == PaymentStatus.paid)
				.map(payment -> payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal remaining = total.subtract(amountPaid).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
		appointment.setTotalAmount(total);
		appointment.setDepositAmount(deposit);
		appointment.setAmountPaid(amountPaid.min(total).setScale(2, RoundingMode.HALF_UP));
		appointment.setRemainingAmount(remaining);
	}

	private BigDecimal safeAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
	}

	private int roundedDuration(Integer durationMinutes) {
		int duration = durationMinutes == null || durationMinutes <= 0 ? 30 : durationMinutes;
		return ((duration + 29) / 30) * 30;
	}
}
