package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
