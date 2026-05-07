package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {
	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public List<Customer> findAll() {
		return customerRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Customer findById(Integer id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
	}

	public Customer save(CustomerRequests.Create req) {
		Customer customer = new Customer();
		customer.setFullName(req.fullName());
		customer.setPhone(req.phone());
		customer.setEmail(req.email());
		customer.setGender(req.gender());
		return customerRepository.save(customer);
	}

	public Customer update(Integer id, CustomerRequests.Update req) {
		Customer customer = findById(id);
		customer.setFullName(req.fullName());
		customer.setPhone(req.phone());
		customer.setEmail(req.email());
		customer.setGender(req.gender());
		return customerRepository.save(customer);
	}

	public void delete(Integer id) {
		if (!customerRepository.existsById(id)) {
			throw new ResourceNotFoundException("Customer not found with id: " + id);
		}
		customerRepository.deleteById(id);
	}
}
