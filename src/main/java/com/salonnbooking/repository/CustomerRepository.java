package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
