package com.salonnbooking.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.ServiceRepository;

@Service
@Transactional
public class ServiceService {
	private final ServiceRepository serviceRepository;

	public ServiceService(ServiceRepository serviceRepository) {
		this.serviceRepository = serviceRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceEntity> findAll() {
		return serviceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
	}

	@Transactional(readOnly = true)
	public List<ServiceEntity> findAllActive() {
		return serviceRepository.findByIsActiveTrue();
	}

	@Transactional(readOnly = true)
	public ServiceEntity findById(Integer id) {
		return serviceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
	}

	public ServiceEntity save(ServiceRequests.Create req) {
		ServiceEntity service = new ServiceEntity();
		service.setName(req.name());
		service.setPrice(req.price());
		service.setDurationMinutes(req.durationMinutes());
		service.setDescription(req.description());
		service.setIsActive(req.isActive() != null ? req.isActive() : true);
		return serviceRepository.save(service);
	}

	public ServiceEntity update(Integer id, ServiceRequests.Update req) {
		ServiceEntity service = findById(id);
		service.setName(req.name());
		service.setPrice(req.price());
		service.setDurationMinutes(req.durationMinutes());
		service.setDescription(req.description());
		service.setIsActive(req.isActive() != null ? req.isActive() : true);
		return serviceRepository.save(service);
	}

	public void delete(Integer id) {
		if (!serviceRepository.existsById(id)) {
			throw new ResourceNotFoundException("Service not found with id: " + id);
		}
		serviceRepository.deleteById(id);
	}
}
