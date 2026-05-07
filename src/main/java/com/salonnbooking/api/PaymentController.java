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

import com.salonnbooking.api.dto.PaymentRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.PaymentRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
	private final PaymentRepository paymentRepository;
	private final AppointmentRepository appointmentRepository;

	public PaymentController(PaymentRepository paymentRepository, AppointmentRepository appointmentRepository) {
		this.paymentRepository = paymentRepository;
		this.appointmentRepository = appointmentRepository;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<PaymentRequests.Response> list() {
		return paymentRepository.findAll().stream().map(PaymentRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public PaymentRequests.Response get(@PathVariable Integer id) {
		Payment payment = paymentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
		return PaymentRequests.Response.from(payment);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PaymentRequests.Response create(@Valid @RequestBody PaymentRequests.Create req) {
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid appointmentId"));

		Payment payment = new Payment();
		payment.setAppointment(appointment);
		payment.setAmount(req.amount());
		payment.setPaymentMethod(req.paymentMethod());
		payment.setPaymentStatus(req.paymentStatus() != null ? req.paymentStatus() : PaymentStatus.unpaid);
		payment.setPaidAt(req.paidAt());
		return PaymentRequests.Response.from(paymentRepository.save(payment));
	}

	@PutMapping("/{id}")
	public PaymentRequests.Response update(@PathVariable Integer id, @Valid @RequestBody PaymentRequests.Update req) {
		Payment payment = paymentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid appointmentId"));

		payment.setAppointment(appointment);
		payment.setAmount(req.amount());
		payment.setPaymentMethod(req.paymentMethod());
		payment.setPaymentStatus(req.paymentStatus());
		payment.setPaidAt(req.paidAt());
		return PaymentRequests.Response.from(paymentRepository.save(payment));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		if (!paymentRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
		}
		paymentRepository.deleteById(id);
	}
}
