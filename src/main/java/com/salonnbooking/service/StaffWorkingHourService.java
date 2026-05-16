package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.domain.StaffWorkingHour;
import com.salonnbooking.repository.StaffWorkingHourRepository;

import jakarta.persistence.EntityManager;

@Service
public class StaffWorkingHourService extends BaseCrudService<StaffWorkingHour> {

    public StaffWorkingHourService(StaffWorkingHourRepository repository, EntityManager entityManager) {
        super(repository, entityManager, StaffWorkingHour.class);
    }
}
