package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {
}
