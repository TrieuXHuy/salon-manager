package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
