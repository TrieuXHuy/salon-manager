package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByAppointmentIdAndCustomerId(Long appointmentId, Long customerId);

    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Review> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    List<Review> findByStaffIdOrderByCreatedAtDesc(Long staffId);
}
