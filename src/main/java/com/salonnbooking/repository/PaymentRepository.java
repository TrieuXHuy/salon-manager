package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByAppointmentId(Long appointmentId);

    List<Payment> findByAppointmentIdIn(List<Long> appointmentIds);
}
