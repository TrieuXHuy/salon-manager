package com.salonnbooking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
	Optional<Customer> findByPhone(String phone);

	Optional<Customer> findByUsername(String username);

	boolean existsByUsername(String username);
}
