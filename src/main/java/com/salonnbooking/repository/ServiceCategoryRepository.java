package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.ServiceCategory;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
}
