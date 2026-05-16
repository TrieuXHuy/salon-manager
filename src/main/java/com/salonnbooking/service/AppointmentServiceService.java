package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.repository.AppointmentServiceRepository;

import jakarta.persistence.EntityManager;

@Service
public class AppointmentServiceService extends BaseCrudService<com.salonnbooking.domain.AppointmentService> {

    public AppointmentServiceService(AppointmentServiceRepository repository, EntityManager entityManager) {
        super(repository, entityManager, com.salonnbooking.domain.AppointmentService.class);
    }
}
