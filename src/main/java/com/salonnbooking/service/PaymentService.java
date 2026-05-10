package com.salonnbooking.service;

import java.math.BigDecimal;
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
		Appointment appointment = findAppointment(req.appointmentId());
		validateAppointmentForPayment(appointment);

		Payment payment = new Payment();
		payment.setAppointment(appointment);
		apply(payment, appointment, req.subtotal(), req.discountAmount(), req.finalAmount(), req.paymentMethod(),
				req.paymentStatus() != null ? req.paymentStatus() : PaymentStatus.UNPAID, req.paidAt());
		return paymentRepository.save(payment);
	}

	public Payment update(Integer id, PaymentRequests.Update req) {
		Payment payment = findById(id);
		Appointment appointment = findAppointment(req.appointmentId());
		validateAppointmentForPayment(appointment);

		payment.setAppointment(appointment);
		apply(payment, appointment, req.subtotal(), req.discountAmount(), req.finalAmount(), req.paymentMethod(),
				req.paymentStatus(), req.paidAt());
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
		payment.setPaymentStatus(PaymentStatus.PAID);
		if (payment.getPaidAt() == null) {
			payment.setPaidAt(LocalDateTime.now());
		}
		return paymentRepository.save(payment);
	}

	private Appointment findAppointment(Integer appointmentId) {
		return appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
	}

	private void validateAppointmentForPayment(Appointment appointment) {
		if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
			throw new IllegalArgumentException("Only completed appointments can be paid");
		}
	}

	private void apply(Payment payment, Appointment appointment, BigDecimal subtotal, BigDecimal discountAmount,
			BigDecimal finalAmount, com.salonnbooking.domain.PaymentMethod paymentMethod, PaymentStatus paymentStatus,
			LocalDateTime paidAt) {
		BigDecimal calculatedSubtotal = appointment.getAppointmentServices().stream()
				.map(item -> item.getPrice() == null ? BigDecimal.ZERO : item.getPrice())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal appliedSubtotal = subtotal != null ? subtotal : calculatedSubtotal;
		BigDecimal appliedDiscount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
		BigDecimal appliedFinal = finalAmount != null ? finalAmount : appliedSubtotal.subtract(appliedDiscount);

		payment.setSubtotal(appliedSubtotal);
		payment.setDiscountAmount(appliedDiscount);
		payment.setFinalAmount(appliedFinal);
		payment.setPaymentMethod(paymentMethod);
		payment.setPaymentStatus(paymentStatus);
		payment.setPaidAt(paymentStatus == PaymentStatus.PAID ? (paidAt != null ? paidAt : LocalDateTime.now()) : paidAt);
	}
}
