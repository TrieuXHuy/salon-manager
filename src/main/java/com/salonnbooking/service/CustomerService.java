package com.salonnbooking.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
		return customerRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
	}

	@Transactional(readOnly = true)
	public Customer findById(Integer id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
	}

	@Transactional(readOnly = true)
	public Customer findProfile(String username) {
		return customerRepository.findByUsername(normalizeUsername(username))
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for username: " + username));
	}

	public Customer createPendingProfile(String username) {
		String normalized = normalizeUsername(username);
		if (customerRepository.existsByUsername(normalized)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer profile already exists");
		}
		Customer customer = new Customer();
		customer.setUsername(normalized);
		return customerRepository.save(customer);
	}

	public void syncProfileUsername(String oldUsername, String newUsername, boolean shouldHaveProfile) {
		String normalizedNew = normalizeUsername(newUsername);
		Customer existing = customerRepository.findByUsername(oldUsername == null ? "" : oldUsername.trim()).orElse(null);
		if (existing != null) {
			customerRepository.findByUsername(normalizedNew)
					.filter(other -> !other.getId().equals(existing.getId()))
					.ifPresent(other -> {
						throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer profile already exists");
					});
			existing.setUsername(normalizedNew);
			customerRepository.save(existing);
		} else if (shouldHaveProfile && !customerRepository.existsByUsername(normalizedNew)) {
			createPendingProfile(normalizedNew);
		}
	}

	public void unlinkProfileUsername(String username) {
		customerRepository.findByUsername(username == null ? "" : username.trim()).ifPresent(customer -> {
			customer.setUsername(null);
			customerRepository.save(customer);
		});
	}

	public Customer save(CustomerRequests.Create req) {
		Customer customer = new Customer();
		customer.setFullName(req.fullName());
		customer.setPhone(req.phone());
		customer.setEmail(req.email());
		customer.setGender(req.gender());
		customer.setNote(req.note());
		return customerRepository.save(customer);
	}

	public Customer completeProfile(CustomerRequests.CompleteProfile req) {
		String username = normalizeUsername(req.username());
		String phone = normalizePhone(req.phone());
		Customer profile = customerRepository.findByUsername(username).orElse(null);
		Customer byPhone = customerRepository.findByPhone(phone).orElse(null);

		if (byPhone != null && byPhone.getUsername() != null && !byPhone.getUsername().equals(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already belongs to another account");
		}

		Customer target = profile;
		if (byPhone != null && byPhone.getUsername() == null) {
			target = byPhone;
			target.setUsername(username);
			if (profile != null && !profile.getId().equals(byPhone.getId())) {
				customerRepository.delete(profile);
			}
		}
		if (target == null) {
			target = new Customer();
			target.setUsername(username);
		}

		target.setFullName(req.fullName().trim());
		target.setPhone(phone);
		target.setEmail(req.email());
		target.setGender(req.gender());
		target.setNote(req.note());
		return customerRepository.save(target);
	}

	public Customer update(Integer id, CustomerRequests.Update req) {
		Customer customer = findById(id);
		customer.setFullName(req.fullName());
		customer.setPhone(req.phone());
		customer.setEmail(req.email());
		customer.setGender(req.gender());
		customer.setLoyaltyPoints(req.loyaltyPoints());
		customer.setNote(req.note());
		return customerRepository.save(customer);
	}

	public void delete(Integer id) {
		if (!customerRepository.existsById(id)) {
			throw new ResourceNotFoundException("Customer not found with id: " + id);
		}
		customerRepository.deleteById(id);
	}

	private String normalizeUsername(String username) {
		if (username == null || username.trim().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
		}
		return username.trim();
	}

	private String normalizePhone(String phone) {
		if (phone == null || phone.trim().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is required");
		}
		return phone.trim();
	}
}
