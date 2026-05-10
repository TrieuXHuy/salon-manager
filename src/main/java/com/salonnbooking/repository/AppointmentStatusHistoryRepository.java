package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.AppointmentStatusHistory;

public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Integer> {
	List<AppointmentStatusHistory> findByAppointmentIdOrderByChangedAtAsc(Integer appointmentId);
}
