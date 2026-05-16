package com.salonnbooking.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByStaffIdAndAppointmentStartLessThanAndAppointmentEndGreaterThanAndStatusIn(
            Long staffId,
            LocalDateTime endExclusive,
            LocalDateTime startExclusive,
            Collection<AppointmentStatus> statuses);

    List<Appointment> findByCustomerIdOrderByAppointmentStartDesc(Long customerId);

    List<Appointment> findByCustomerIdAndStatusOrderByAppointmentStartDesc(Long customerId, AppointmentStatus status);
}
