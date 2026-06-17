package com.salonnbooking.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.domain.User;
import com.salonnbooking.domain.UserRole;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.CustomerRepository;
import com.salonnbooking.repository.UserRepository;

@Service
@Transactional
public class CustomerService {
	private static final String DEFAULT_CUSTOMER_PASSWORD = "123456";

	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;

	public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
		this.customerRepository = customerRepository;
		this.userRepository = userRepository;
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
		String username = uniqueCustomerUsername(req.fullName());

		User user = new User();
		user.setUsername(username);
		user.setPassword(DEFAULT_CUSTOMER_PASSWORD);
		user.setRole(UserRole.CUSTOMER);
		userRepository.save(user);

		Customer customer = new Customer();
		customer.setUsername(username);
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
			// Dùng lại record theo số điện thoại để không tạo trùng customer.
			target = byPhone;
			target.setUsername(username);
			if (profile != null && !profile.getId().equals(byPhone.getId())) {
				// Xóa record placeholder theo username sau khi đã gộp xong.
				customerRepository.delete(profile);
			}
		}
		if (target == null) {
			// Không có record nào khớp thì tạo customer mới.
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

	private String uniqueCustomerUsername(String fullName) {
		String base = normalizeFullNameForUsername(fullName);
		String candidate = base;
		int suffix = 2;
		while (userRepository.existsByUsername(candidate) || customerRepository.existsByUsername(candidate)) {
			String suffixText = String.valueOf(suffix++);
			int maxBaseLength = Math.max(1, 50 - suffixText.length());
			candidate = base.substring(0, Math.min(base.length(), maxBaseLength)) + suffixText;
		}
		return candidate;
	}

	private String normalizeFullNameForUsername(String fullName) {
		String value = fullName == null ? "" : fullName.trim().toLowerCase(Locale.ROOT);
		value = Normalizer.normalize(value, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replace("đ", "d");
		value = value.replaceAll("[^a-z0-9]", "");
		if (value.isBlank()) {
			value = "customer";
		}
		return value.length() > 50 ? value.substring(0, 50) : value;
	}

	private String normalizePhone(String phone) {
		if (phone == null || phone.trim().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is required");
		}
		return phone.trim();
	}
}
