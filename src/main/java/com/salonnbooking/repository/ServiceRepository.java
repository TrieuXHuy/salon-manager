package com.salonnbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findByName(String name);

    List<Service> findByCategoryId(Long categoryId);

    List<Service> findByIsActiveTrue();

    List<Service> findByIsActiveTrueAndCategoryId(Long categoryId);
}
