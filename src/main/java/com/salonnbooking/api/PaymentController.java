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

import com.salonnbooking.api.dto.PaymentRequests;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<PaymentRequests.Response> list() {
		return paymentService.findAll().stream().map(PaymentRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public PaymentRequests.Response get(@PathVariable Integer id) {
		Payment payment = paymentService.findById(id);
		return PaymentRequests.Response.from(payment);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PaymentRequests.Response create(@Valid @RequestBody PaymentRequests.Create req) {
		Payment payment = paymentService.save(req);
		return PaymentRequests.Response.from(payment);
	}

	@PutMapping("/{id}")
	public PaymentRequests.Response update(@PathVariable Integer id, @Valid @RequestBody PaymentRequests.Update req) {
		Payment payment = paymentService.update(id, req);
		return PaymentRequests.Response.from(payment);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		paymentService.delete(id);
	}

	@PostMapping("/{id}/mark-paid")
	public PaymentRequests.Response markAsPaid(@PathVariable Integer id) {
		Payment payment = paymentService.markAsPaid(id);
		return PaymentRequests.Response.from(payment);
	}
}
