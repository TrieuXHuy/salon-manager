package com.salonnbooking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
	List<Appointment> findByCustomerId(Integer customerId);

	List<Appointment> findByStatus(AppointmentStatus status);

	List<Appointment> findByEmployeeId(Integer employeeId);

	@Query("SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :startTime AND :endTime")
	List<Appointment> findAppointmentsBetween(@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime);

	@Query("""
			SELECT a FROM Appointment a
			WHERE a.employee.id = :employeeId
			  AND a.status NOT IN :ignoredStatuses
			  AND (:excludeId IS NULL OR a.id <> :excludeId)
			  AND a.appointmentTime < :endTime
			  AND a.estimatedEndTime > :startTime
			""")
	List<Appointment> findConflictingAppointments(
			@Param("employeeId") Integer employeeId,
			@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime,
			@Param("ignoredStatuses") List<AppointmentStatus> ignoredStatuses,
			@Param("excludeId") Integer excludeId);
}
