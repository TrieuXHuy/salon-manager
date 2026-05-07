package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
	List<Payment> findByAppointmentId(Integer appointmentId);

	List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}
