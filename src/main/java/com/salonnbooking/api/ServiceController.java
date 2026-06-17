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

import com.salonnbooking.api.dto.ServiceRequests;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.service.ServiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
	private final ServiceService serviceService;

	public ServiceController(ServiceService serviceService) {
		this.serviceService = serviceService;
	}

	/** Lấy toàn bộ danh sách dịch vụ. */
	@GetMapping
	public List<ServiceRequests.Response> list() {
		return serviceService.findAll().stream().map(ServiceRequests.Response::from).toList();
	}

	/** Lấy danh sách dịch vụ đang hoạt động. */
	@GetMapping("/active")
	public List<ServiceRequests.Response> listActive() {
		return serviceService.findAllActive().stream().map(ServiceRequests.Response::from).toList();
	}

	/** Lấy chi tiết dịch vụ theo id. */
	@GetMapping("/{id}")
	public ServiceRequests.Response get(@PathVariable Integer id) {
		ServiceEntity service = serviceService.findById(id);
		return ServiceRequests.Response.from(service);
	}

	/** Tạo mới một dịch vụ. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ServiceRequests.Response create(@Valid @RequestBody ServiceRequests.Create req) {
		ServiceEntity service = serviceService.save(req);
		return ServiceRequests.Response.from(service);
	}

	/** Cập nhật thông tin dịch vụ theo id. */
	@PutMapping("/{id}")
	public ServiceRequests.Response update(@PathVariable Integer id, @Valid @RequestBody ServiceRequests.Update req) {
		ServiceEntity service = serviceService.update(id, req);
		return ServiceRequests.Response.from(service);
	}

	/** Xóa dịch vụ theo id. */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		serviceService.delete(id);
	}
}
