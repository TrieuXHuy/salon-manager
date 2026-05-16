package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
