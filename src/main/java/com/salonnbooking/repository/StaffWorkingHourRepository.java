package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.StaffWorkingHour;

public interface StaffWorkingHourRepository extends JpaRepository<StaffWorkingHour, Long> {
}
