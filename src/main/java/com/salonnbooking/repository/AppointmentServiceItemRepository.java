package com.salonnbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.AppointmentServiceItem;

public interface AppointmentServiceItemRepository extends JpaRepository<AppointmentServiceItem, Integer> {
	List<AppointmentServiceItem> findByAppointmentId(Integer appointmentId);
}
