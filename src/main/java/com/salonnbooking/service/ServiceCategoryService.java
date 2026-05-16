package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.domain.ServiceCategory;
import com.salonnbooking.repository.ServiceCategoryRepository;

import jakarta.persistence.EntityManager;

@Service
public class ServiceCategoryService extends BaseCrudService<ServiceCategory> {

    public ServiceCategoryService(ServiceCategoryRepository repository, EntityManager entityManager) {
        super(repository, entityManager, ServiceCategory.class);
    }
}
