package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping
	public List<CustomerRequests.Response> list() {
		return customerService.findAll().stream().map(CustomerRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	public CustomerRequests.Response get(@PathVariable Integer id) {
		Customer customer = customerService.findById(id);
		return CustomerRequests.Response.from(customer);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerRequests.Response create(@Valid @RequestBody CustomerRequests.Create req) {
		Customer customer = customerService.save(req);
		return CustomerRequests.Response.from(customer);
	}

	@PutMapping("/{id}")
	public CustomerRequests.Response update(@PathVariable Integer id, @Valid @RequestBody CustomerRequests.Update req) {
		Customer customer = customerService.update(id, req);
		return CustomerRequests.Response.from(customer);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		customerService.delete(id);
	}
}
