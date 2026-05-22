package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.ServiceRoom;

public interface ServiceRoomRepository extends JpaRepository<ServiceRoom, Integer> {
	List<ServiceRoom> findByIsActiveTrueOrderByIdAsc();
}
