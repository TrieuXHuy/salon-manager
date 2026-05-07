package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {
	List<ServiceEntity> findByIsActiveTrue();
}
