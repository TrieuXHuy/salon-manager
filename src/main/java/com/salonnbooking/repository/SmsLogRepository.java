package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.SmsLog;
import com.salonnbooking.domain.SmsStatus;

public interface SmsLogRepository extends JpaRepository<SmsLog, Integer> {
	List<SmsLog> findByAppointmentId(Integer appointmentId);

	List<SmsLog> findByStatus(SmsStatus status);
}
