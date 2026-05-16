package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.AppointmentService;

public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Long> {

    List<AppointmentService> findByAppointmentId(Long appointmentId);
}
