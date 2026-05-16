package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.domain.StaffService;
import com.salonnbooking.repository.StaffServiceRepository;

import jakarta.persistence.EntityManager;

@Service
public class StaffServiceService extends BaseCrudService<StaffService> {

    public StaffServiceService(StaffServiceRepository repository, EntityManager entityManager) {
        super(repository, entityManager, StaffService.class);
    }
}
