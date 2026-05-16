package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.StaffWorkingHour;

public interface StaffWorkingHourRepository extends JpaRepository<StaffWorkingHour, Long> {

    List<StaffWorkingHour> findByStaffId(Long staffId);
}
