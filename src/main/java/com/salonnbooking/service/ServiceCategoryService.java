package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ServiceCategoryDtos;
import com.salonnbooking.domain.ServiceCategory;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.ServiceCategoryRepository;

import jakarta.persistence.EntityManager;

@Service
public class ServiceCategoryService extends BaseCrudService<ServiceCategory> {

    private final ServiceCategoryRepository repository;

    public ServiceCategoryService(ServiceCategoryRepository repository, EntityManager entityManager) {
        super(repository, entityManager, ServiceCategory.class);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ServiceCategoryDtos.Response> getAdminCategories() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ServiceCategoryDtos.Response create(ServiceCategoryDtos.UpsertRequest request) {
        ServiceCategory category = ServiceCategory.builder()
                .name(request.name())
                .description(request.description())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        return toResponse(repository.save(category));
    }

    @Transactional
    public ServiceCategoryDtos.Response update(Long id, ServiceCategoryDtos.UpsertRequest request) {
        ServiceCategory category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory not found with id: " + id));
        category.setName(request.name());
        category.setDescription(request.description());
        if (request.isActive() != null) {
            category.setIsActive(request.isActive());
        }
        return toResponse(repository.save(category));
    }

    @Transactional
    public ServiceCategoryDtos.Response toggleActive(Long id) {
        ServiceCategory category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory not found with id: " + id));
        category.setIsActive(!Boolean.TRUE.equals(category.getIsActive()));
        return toResponse(repository.save(category));
    }

    public ServiceCategoryDtos.Response toResponse(ServiceCategory category) {
        return new ServiceCategoryDtos.Response(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIsActive());
    }
}
