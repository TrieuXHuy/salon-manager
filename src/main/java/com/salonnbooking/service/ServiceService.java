package com.salonnbooking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.exception.ResourceNotFoundException;

import jakarta.persistence.EntityManager;

@org.springframework.stereotype.Service
public class ServiceService extends BaseCrudService<com.salonnbooking.domain.Service> {

    private final com.salonnbooking.repository.ServiceRepository repository;
    private final com.salonnbooking.repository.ServiceCategoryRepository serviceCategoryRepository;

    public ServiceService(
            com.salonnbooking.repository.ServiceRepository repository,
            com.salonnbooking.repository.ServiceCategoryRepository serviceCategoryRepository,
            EntityManager entityManager) {
        super(repository, entityManager, com.salonnbooking.domain.Service.class);
        this.repository = repository;
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.Response> getAdminServices(Long categoryId) {
        List<com.salonnbooking.domain.Service> services = categoryId == null
                ? repository.findAll()
                : repository.findByCategoryId(categoryId);
        return services.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.Response> getPublicServices(Long categoryId) {
        List<com.salonnbooking.domain.Service> services = categoryId == null
                ? repository.findByIsActiveTrue()
                : repository.findByIsActiveTrueAndCategoryId(categoryId);
        return services.stream()
                .filter(service -> service.getCategory() == null || Boolean.TRUE.equals(service.getCategory().getIsActive()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceDtos.Response getPublicServiceById(Long id) {
        com.salonnbooking.domain.Service service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        if (!Boolean.TRUE.equals(service.getIsActive())) {
            throw new ResourceNotFoundException("Service not found with id: " + id);
        }
        if (service.getCategory() != null && !Boolean.TRUE.equals(service.getCategory().getIsActive())) {
            throw new ResourceNotFoundException("Service not found with id: " + id);
        }
        return toResponse(service);
    }

    @Transactional
    public ServiceDtos.Response create(ServiceDtos.UpsertRequest request) {
        com.salonnbooking.domain.ServiceCategory category = serviceCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory not found with id: " + request.categoryId()));

        com.salonnbooking.domain.Service service = com.salonnbooking.domain.Service.builder()
                .category(category)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .durationMinutes(request.durationMinutes())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .createdAt(LocalDateTime.now())
                .build();
        return toResponse(repository.save(service));
    }

    @Transactional
    public ServiceDtos.Response update(Long id, ServiceDtos.UpsertRequest request) {
        com.salonnbooking.domain.Service service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        com.salonnbooking.domain.ServiceCategory category = serviceCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory not found with id: " + request.categoryId()));

        service.setCategory(category);
        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        if (request.isActive() != null) {
            service.setIsActive(request.isActive());
        }
        return toResponse(repository.save(service));
    }

    @Transactional
    public ServiceDtos.Response toggleActive(Long id) {
        com.salonnbooking.domain.Service service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        service.setIsActive(!Boolean.TRUE.equals(service.getIsActive()));
        return toResponse(repository.save(service));
    }

    public ServiceDtos.Response toResponse(com.salonnbooking.domain.Service service) {
        return new ServiceDtos.Response(
                service.getId(),
                service.getCategory() != null ? service.getCategory().getId() : null,
                service.getCategory() != null ? service.getCategory().getName() : null,
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                service.getIsActive(),
                service.getCreatedAt());
    }
}
