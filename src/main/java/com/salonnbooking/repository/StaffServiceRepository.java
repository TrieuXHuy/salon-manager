package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.StaffService;

public interface StaffServiceRepository extends JpaRepository<StaffService, Long> {
}
