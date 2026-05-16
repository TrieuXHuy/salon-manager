package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByCategoryId(Long categoryId);

    List<Service> findByIsActiveTrue();

    List<Service> findByIsActiveTrueAndCategoryId(Long categoryId);
}
