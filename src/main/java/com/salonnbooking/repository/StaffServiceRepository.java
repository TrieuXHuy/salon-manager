package com.salonnbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.StaffService;

public interface StaffServiceRepository extends JpaRepository<StaffService, Long> {

    List<StaffService> findByStaffId(Long staffId);

    List<StaffService> findByServiceId(Long serviceId);

    Optional<StaffService> findByStaffIdAndServiceId(Long staffId, Long serviceId);
}
