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
import org.springframework.web.server.ResponseStatusException;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.repository.CustomerRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
	private final CustomerRepository customerRepository;

	public CustomerController(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@GetMapping
	public List<CustomerRequests.Response> list() {
		return customerRepository.findAll().stream().map(CustomerRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	public CustomerRequests.Response get(@PathVariable Integer id) {
		Customer customer = customerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
		return CustomerRequests.Response.from(customer);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerRequests.Response create(@Valid @RequestBody CustomerRequests.Create req) {
		Customer customer = new Customer();
		customer.setFullName(req.fullName());
		customer.setPhone(req.phone());
		customer.setEmail(req.email());
		customer.setGender(req.gender());
		return CustomerRequests.Response.from(customerRepository.save(customer));
	}

	@PutMapping("/{id}")
	public CustomerRequests.Response update(@PathVariable Integer id, @Valid @RequestBody CustomerRequests.Update req) {
		Customer customer = customerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
		customer.setFullName(req.fullName());
		customer.setPhone(req.phone());
		customer.setEmail(req.email());
		customer.setGender(req.gender());
		return CustomerRequests.Response.from(customerRepository.save(customer));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		if (!customerRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
		}
		customerRepository.deleteById(id);
	}
}
