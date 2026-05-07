package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.repository.ServiceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
	private final ServiceRepository serviceRepository;

	public ServiceController(ServiceRepository serviceRepository) {
		this.serviceRepository = serviceRepository;
	}

	@GetMapping
	public List<ServiceRequests.Response> list() {
		return serviceRepository.findAll().stream().map(ServiceRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	public ServiceRequests.Response get(@PathVariable Integer id) {
		ServiceEntity service = serviceRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
		return ServiceRequests.Response.from(service);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ServiceRequests.Response create(@Valid @RequestBody ServiceRequests.Create req) {
		ServiceEntity service = new ServiceEntity();
		service.setName(req.name());
		service.setPrice(req.price());
		service.setDurationMinutes(req.durationMinutes());
		service.setDescription(req.description());
		if (req.isActive() != null) {
			service.setIsActive(req.isActive());
		}
		return ServiceRequests.Response.from(serviceRepository.save(service));
	}

	@PutMapping("/{id}")
	public ServiceRequests.Response update(@PathVariable Integer id, @Valid @RequestBody ServiceRequests.Update req) {
		ServiceEntity service = serviceRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
		service.setName(req.name());
		service.setPrice(req.price());
		service.setDurationMinutes(req.durationMinutes());
		service.setDescription(req.description());
		service.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);
		return ServiceRequests.Response.from(serviceRepository.save(service));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		if (!serviceRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
		}
		serviceRepository.deleteById(id);
	}
}
