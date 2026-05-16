package com.salonnbooking.service;

import jakarta.persistence.EntityManager;

@org.springframework.stereotype.Service
public class ServiceService extends BaseCrudService<com.salonnbooking.domain.Service> {

    public ServiceService(com.salonnbooking.repository.ServiceRepository repository, EntityManager entityManager) {
        super(repository, entityManager, com.salonnbooking.domain.Service.class);
    }
}
