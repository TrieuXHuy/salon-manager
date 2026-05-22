package com.salonnbooking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.PaymentRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.PaymentRepository;

@Service
@Transactional
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final AppointmentRepository appointmentRepository;

	public PaymentService(PaymentRepository paymentRepository, AppointmentRepository appointmentRepository) {
		this.paymentRepository = paymentRepository;
		this.appointmentRepository = appointmentRepository;
	}

	@Transactional(readOnly = true)
	public List<Payment> findAll() {
		return paymentRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Payment findById(Integer id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
	}

	public Payment save(PaymentRequests.Create req) {
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + req.appointmentId()));
		boolean alreadyPaid = paymentRepository.findByAppointmentId(req.appointmentId()).stream()
				.anyMatch(payment -> payment.getPaymentStatus() == PaymentStatus.paid);
		if (alreadyPaid) {
			throw new IllegalArgumentException("Appointment has already been paid");
		}

		Payment payment = new Payment();
		payment.setAppointment(appointment);
		payment.setAmount(req.amount());
		payment.setPaymentMethod(req.paymentMethod());
		payment.setPaymentStatus(req.paymentStatus() != null ? req.paymentStatus() : PaymentStatus.unpaid);
		payment.setPaidAt(req.paidAt());
		updateAppointmentStatusWhenPaid(appointment, payment.getPaymentStatus());
		return paymentRepository.save(payment);
	}

	public Payment update(Integer id, PaymentRequests.Update req) {
		Payment payment = findById(id);
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + req.appointmentId()));

		payment.setAppointment(appointment);
		payment.setAmount(req.amount());
		payment.setPaymentMethod(req.paymentMethod());
		payment.setPaymentStatus(req.paymentStatus());
		payment.setPaidAt(req.paidAt());
		updateAppointmentStatusWhenPaid(appointment, payment.getPaymentStatus());
		return paymentRepository.save(payment);
	}

	public void delete(Integer id) {
		if (!paymentRepository.existsById(id)) {
			throw new ResourceNotFoundException("Payment not found with id: " + id);
		}
		paymentRepository.deleteById(id);
	}

	public Payment markAsPaid(Integer id) {
		Payment payment = findById(id);
		payment.setPaymentStatus(PaymentStatus.paid);
		payment.setPaidAt(LocalDateTime.now());
		updateAppointmentStatusWhenPaid(payment.getAppointment(), payment.getPaymentStatus());
		return paymentRepository.save(payment);
	}

	private void updateAppointmentStatusWhenPaid(Appointment appointment, PaymentStatus paymentStatus) {
		if (paymentStatus == PaymentStatus.paid) {
			appointment.setStatus(AppointmentStatus.paid);
			int earnedPoints = calculateLoyaltyPoints(appointment);
			appointment.getCustomer().setLoyaltyPoints(
					appointment.getCustomer().getLoyaltyPoints() + earnedPoints);
			appointmentRepository.save(appointment);
		}
	}

	private int calculateLoyaltyPoints(Appointment appointment) {
		BigDecimal price = appointment.getService().getPrice();
		if (price == null) {
			return 0;
		}
		return price.divide(BigDecimal.valueOf(10000), 0, RoundingMode.DOWN).intValue();
	}
}
