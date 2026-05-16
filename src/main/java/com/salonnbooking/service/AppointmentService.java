package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.domain.Appointment;
import com.salonnbooking.repository.AppointmentRepository;

import jakarta.persistence.EntityManager;

@Service
public class AppointmentService extends BaseCrudService<Appointment> {

    public AppointmentService(AppointmentRepository repository, EntityManager entityManager) {
        super(repository, entityManager, Appointment.class);
    }
}
