package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.SmsLog;

public interface SmsLogRepository extends JpaRepository<SmsLog, Integer> {
}
