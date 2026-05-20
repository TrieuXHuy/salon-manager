package com.salonnbooking.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByCustomerId(Long customerId);

    boolean existsByStaffId(Long staffId);

    List<Appointment> findByStaffIdAndAppointmentStartLessThanAndAppointmentEndGreaterThanAndStatusIn(
            Long staffId,
            LocalDateTime endExclusive,
            LocalDateTime startExclusive,
            Collection<AppointmentStatus> statuses);

    List<Appointment> findByCustomerIdOrderByAppointmentStartDesc(Long customerId);

    List<Appointment> findByCustomerIdAndStatusOrderByAppointmentStartDesc(Long customerId, AppointmentStatus status);

    List<Appointment> findByStaffIdOrderByAppointmentStartDesc(Long staffId);

    List<Appointment> findByStaffIdAndStatusOrderByAppointmentStartDesc(Long staffId, AppointmentStatus status);

    List<Appointment> findByStaffIdAndAppointmentStartBetweenOrderByAppointmentStartDesc(
            Long staffId,
            LocalDateTime from,
            LocalDateTime to);

    List<Appointment> findByStaffIdAndAppointmentStartBetweenAndStatusOrderByAppointmentStartDesc(
            Long staffId,
            LocalDateTime from,
            LocalDateTime to,
            AppointmentStatus status);

    List<Appointment> findByAppointmentStartBetweenOrderByAppointmentStartDesc(LocalDateTime from, LocalDateTime to);

    List<Appointment> findByAppointmentStartBetweenAndStaffIdOrderByAppointmentStartDesc(
            LocalDateTime from,
            LocalDateTime to,
            Long staffId);

    List<Appointment> findByAppointmentStartBetweenAndCustomerIdOrderByAppointmentStartDesc(
            LocalDateTime from,
            LocalDateTime to,
            Long customerId);

    List<Appointment> findByAppointmentStartBetweenAndStatusOrderByAppointmentStartDesc(
            LocalDateTime from,
            LocalDateTime to,
            AppointmentStatus status);

    List<Appointment> findByAppointmentStartBetweenAndStaffIdAndCustomerIdOrderByAppointmentStartDesc(
            LocalDateTime from,
            LocalDateTime to,
            Long staffId,
            Long customerId);

    List<Appointment> findByAppointmentStartBetweenAndStaffIdAndStatusOrderByAppointmentStartDesc(
            LocalDateTime from,
            LocalDateTime to,
            Long staffId,
            AppointmentStatus status);

    List<Appointment> findByAppointmentStartBetweenAndCustomerIdAndStatusOrderByAppointmentStartDesc(
            LocalDateTime from,
            LocalDateTime to,
            Long customerId,
            AppointmentStatus status);

    List<Appointment> findByAppointmentStartBetweenAndStaffIdAndCustomerIdAndStatusOrderByAppointmentStartDesc(
            LocalDateTime from,
            LocalDateTime to,
            Long staffId,
            Long customerId,
            AppointmentStatus status);
}
